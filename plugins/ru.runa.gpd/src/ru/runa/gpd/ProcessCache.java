package ru.runa.gpd;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.GlobalSectionDefinition;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class ProcessCache {
    private static Map<IFile, ProcessDefinition> CACHE_BY_FILE = new HashMap<IFile, ProcessDefinition>();
    private static Map<String, ProcessDefinition> CACHE_BY_NAME = new HashMap<String, ProcessDefinition>();
    static {
        try {
            for (IFile file : IOUtils.getAllProcessDefinitionFiles()) {
                try {
                    ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                    cacheProcessDefinition(file, definition);
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("parsing process " + file, e);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private static void findSubProcessFiles(IContainer container, List<IFile> result) throws CoreException {
        for (IResource resource : container.members()) {
            if (resource.getName().endsWith(ParContentProvider.PROCESS_DEFINITION_FILE_NAME)
                    && !resource.getName().equals(ParContentProvider.PROCESS_DEFINITION_FILE_NAME)) {
                result.add((IFile) resource);
            }
        }
    }

    private static void cacheProcessDefinition(IFile file, ProcessDefinition definition) throws Exception {
        ParContentProvider.readAuxInfo(file, definition);
        CACHE_BY_FILE.put(file, definition);
        CACHE_BY_NAME.put(definition.getName(), definition);
        if (definition instanceof SubprocessDefinition) {
            return;
        }
        List<IFile> subprocessFiles = Lists.newArrayList();
        findSubProcessFiles(file.getParent(), subprocessFiles);
        for (IFile subprocessFile : subprocessFiles) {
            try {
                cacheProcessDefinition(subprocessFile, NodeRegistry.parseProcessDefinition(subprocessFile));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("parsing subprocess " + subprocessFile, e);
            }
        }
    }

    public static synchronized ProcessDefinition newProcessDefinitionWasCreated(IFile file) {
        try {
            ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
            cacheProcessDefinition(file, definition);
            return definition;
        } catch (Exception e) {
            PluginLogger.logError("Parsing process definition failed: " + file.toString(), e);
            return null;
        }
    }

    public static synchronized void processDefinitionWasDeleted(IFile file) {
        try {
            ProcessDefinition definition = CACHE_BY_FILE.remove(file);
            if (definition != null) {
                CACHE_BY_NAME.remove(definition.getName());
                if (!(definition instanceof SubprocessDefinition)) {
                    for (SubprocessDefinition subprocessDefinition : definition.getEmbeddedSubprocesses().values()) {
                        processDefinitionWasDeleted(subprocessDefinition.getFile());
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("Unable to delete process definition from cache: " + file, e);
        }
    }

    public static synchronized Set<ProcessDefinition> getAllProcessDefinitions() {
        return new HashSet<ProcessDefinition>(CACHE_BY_NAME.values());
    }

    public static synchronized List<String> getAllProcessDefinitionNames() {
        List<String> list = new ArrayList<String>(CACHE_BY_NAME.keySet());
        Collections.sort(list);
        return list;
    }

    public static synchronized Map<IFile, ProcessDefinition> getAllProcessDefinitionsMap() {
        return new HashMap<IFile, ProcessDefinition>(CACHE_BY_FILE);
    }

    public static synchronized IFile getProcessDefinitionFile(ProcessDefinition processDefinition) {
        for (Map.Entry<IFile, ProcessDefinition> entry : CACHE_BY_FILE.entrySet()) {
            if (entry.getValue() == processDefinition) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("No file exist for script task " + processDefinition.getName());
    }

    public static synchronized void invalidateProcessDefinition(IFile file) {
        ProcessDefinition definition = CACHE_BY_FILE.remove(file);
        if (definition != null) {
            CACHE_BY_NAME.remove(definition.getName());
            if (file.exists()) {
                ProcessDefinition parsedDefinition = getProcessDefinition(file);
                if (!(definition instanceof SubprocessDefinition)) {
                    parsedDefinition.getEmbeddedSubprocesses().putAll(definition.getEmbeddedSubprocesses());
                }
            }
        }
    }

    public static synchronized ProcessDefinition getProcessDefinition(IFile file) {
        if (!CACHE_BY_FILE.containsKey(file)) {
            try {
                ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                cacheProcessDefinition(file, definition);
            } catch (Exception e) {
                throw new RuntimeException("Parsing process definition failed: " + file, e);
            }
        }
        return CACHE_BY_FILE.get(file);
    }

    public static synchronized ProcessDefinition getFirstProcessDefinition(String name, String desirableProjectName) {
        if (!CACHE_BY_NAME.containsKey(name)) {
            try {
                IFile file = getFirstProcessDefinitionFile(name, desirableProjectName);
                if (file != null) {
                    ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                    cacheProcessDefinition(file, definition);
                }
            } catch (Exception e) {
                PluginLogger.logError("Parsing process definition failed: " + name, e);
                return null;
            }
        }
        return CACHE_BY_NAME.get(name);
    }

    /**
     * Get process definition file or <code>null</code>.
     */
    public static synchronized IFile getFirstProcessDefinitionFile(String processName, String desirableProjectName) {
        try {
            IFile firstFile = null;
            for (IFile file : IOUtils.getAllProcessDefinitionFiles()) {
                if (processName.equals(file.getParent().getName())) {
                    if (desirableProjectName == null) {
                        return file;
                    } else if (file.getProject().getName().equals(desirableProjectName)) {
                        return file;
                    } else {
                        if (firstFile == null) {
                            firstFile = file;
                        }
                    }
                }
            }
            if (firstFile != null) {
                return firstFile;
            }
            PluginLogger.logInfo("No process definition found by name: " + processName);
        } catch (Exception e) {
            PluginLogger.logError("Parsing process definition failed: " + processName, e);
            return null;
        }
        return null;
    }

    public static GlobalSectionDefinition getGlobalProcessDefinition(ProcessDefinition processDefinition) {

        GlobalSectionDefinition definitionToReturn = null;
        int maxLength = 0;
        for (ProcessDefinition definition : getAllProcessDefinitions()) {
            if (definition instanceof GlobalSectionDefinition) {
                IPath globalSectionPath = definition.getFile().getProjectRelativePath();
                IPath processPath = processDefinition.getFile().getProjectRelativePath();
                if (maxLength == 0 && globalSectionPath.toString().startsWith(".")) {
                    definitionToReturn = (GlobalSectionDefinition) definition;
                }
                int length = globalSectionPath.matchingFirstSegments(processPath);
                if (length > maxLength) {
                    maxLength = length;
                    definitionToReturn = (GlobalSectionDefinition) definition;
                }

            }
        }
        return definitionToReturn;
    }

}

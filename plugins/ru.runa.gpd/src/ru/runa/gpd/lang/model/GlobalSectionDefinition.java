package ru.runa.gpd.lang.model;

import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;

public class GlobalSectionDefinition extends ProcessDefinition {

    public GlobalSectionDefinition(IFile file) throws CoreException {
        super(file);
    }

    @Override
    public Swimlane getGlobalSwimlaneByName(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            name = name.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length());
        }
        for (Swimlane swimlane : getGlobalSwimlanes()) {
            if (name.equals(swimlane.getName())) {
                return swimlane;
            }
        }
        return null;
    }

    @Override
    public Variable getGlobalVariableByName(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            name = name.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length());
        }
        for (Variable variable : getGlobalVariables()) {
            if (name.equals(variable.getName())) {
                return variable;
            }
        }
        return null;
    }

    @Override
    public VariableUserType getGlobalUserTypeByName(String name) {
        if (name == null) {
            return null;
        }
        if (name.startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            name = name.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length());
        }
        for (VariableUserType type : getGlobalTypes()) {
            if (name.equals(type.getName())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/glb.gif");
    }

    @Override
    public List<VariableUserType> getVariableUserTypes() {
        return types;
    }

    @Override
    public void removeVariableUserType(VariableUserType type) {
        removeGlobalVariableUserTypeInAllProcesses(type, file.getParent().getParent());
        super.removeVariableUserType(type);
    }

    @Override
    public List<Swimlane> getGlobalSwimlanes() {
        return getSwimlanes();
    }

    @Override
    public List<Variable> getGlobalVariables() {
        List<Variable> variables = getChildren(Variable.class);
        variables.removeAll(getSwimlanes());
        return variables;
    }

    @Override
    public List<VariableUserType> getGlobalTypes() {
        return types;
    }

    @Override
    public void removeGlobalSwimlaneInAllProcesses(Swimlane swimlane, IContainer folder) {
        try {
            for (IResource r : folder.members()) {
                if (r instanceof IFolder) {
                    if (IOUtils.isProcessDefinitionFolder((IFolder) r)) {
                        IFile definitionFile = (IFile) ((IFolder) r).findMember(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                        if (definitionFile != null) {
                            ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                            Swimlane globalSwimlane = definition.getGlobalSwimlaneByName(IOUtils.GLOBAL_OBJECT_PREFIX + swimlane.getName());
                            if (globalSwimlane != null) {
                                globalSwimlane.setGlobal(false);
                            }
                        }
                    }
                    if (r instanceof IContainer) {
                        removeGlobalSwimlaneInAllProcesses(swimlane, (IContainer) r);
                    }
                }
            }
            removeChild(swimlane);
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void removeGlobalVariableInAllProcesses(Variable variable, IContainer folder) {
        try {
            for (IResource r : folder.members()) {
                if (r instanceof IFolder) {
                    if (IOUtils.isProcessDefinitionFolder((IFolder) r)) {
                        IFile definitionFile = (IFile) ((IFolder) r).findMember(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                        if (definitionFile != null) {
                            ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                            Variable globalVariable = definition.getGlobalVariableByName(IOUtils.GLOBAL_OBJECT_PREFIX + variable.getName());
                            if (globalVariable != null) {
                                globalVariable.setGlobal(false);
                            }
                        }
                    }
                    if (r instanceof IContainer) {
                        removeGlobalVariableInAllProcesses(variable, (IContainer) r);
                    }
                }
            }
            removeChild(variable);
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void removeGlobalVariableUserTypeInAllProcesses(VariableUserType type, IContainer folder) {
        try {
            for (IResource r : folder.members()) {
                if (r instanceof IFolder) {
                    if (IOUtils.isProcessDefinitionFolder((IFolder) r)) {
                        IFile definitionFile = (IFile) ((IFolder) r).findMember(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                        if (definitionFile != null) {
                            ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                            VariableUserType globalType = definition.getTypeByName(IOUtils.GLOBAL_OBJECT_PREFIX + type.getName());
                            if (globalType != null) {
                                globalType.setGlobal(false);
                            }
                        }
                    }
                    if (r instanceof IContainer) {
                        removeGlobalVariableUserTypeInAllProcesses(type, (IContainer) r);
                    }
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public String getLabel() {
        return GlobalSectionUtils.getLabel(super.getLabel());
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
    }

    @Override
    public void addVariableUserType(VariableUserType type) {
        if (getTypeByName(type.getName()) == null) {
            super.addVariableUserType(type);
        }
    }

    public void addSwimlane(Swimlane swimlane) {
        if (getSwimlaneByName(swimlane.getName()) == null) {
            addChild(swimlane);
        }
    }

    public void addVariable(Variable variable) {
        if (!VariableUtils.variableExists(variable.getName(), this)) {

            if (variable.getUserType() != null) {
                VariableUserType type = variable.getUserType().getCopyForGlobalPartition();
                addVariableUserType(type);
                variable.setUserType(type);
            }
            addChild(variable);
        }
    }
}

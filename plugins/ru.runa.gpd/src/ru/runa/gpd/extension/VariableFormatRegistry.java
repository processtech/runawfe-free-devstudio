package ru.runa.gpd.extension;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.UserTypeMap;

public class VariableFormatRegistry extends ArtifactRegistry<VariableFormatArtifact> {
    private static final String XML_FILE_NAME = "variableFormats.xml";
    private static final VariableFormatRegistry instance = new VariableFormatRegistry();
    private List<VariableFormatArtifact> filterArtifacts;

    public static VariableFormatRegistry getInstance() {
        return instance;
    }

    public VariableFormatRegistry() {
        super(new ArtifactContentProvider<VariableFormatArtifact>());
    }

    @Override
    protected File getContentFile() {
        return new File(Activator.getPreferencesFolder(), XML_FILE_NAME);
    }

    @Override
    protected void loadDefaults(List<VariableFormatArtifact> list) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.formats").getExtensions();
        Map<String, List<String>> mappingByTypeClass = Maps.newHashMap();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                boolean enabled = Boolean.valueOf(configElement.getAttribute("enabled"));
                String className = configElement.getAttribute("className");
                String label = configElement.getAttribute("label");
                String javaClassName = configElement.getAttribute("javaClassName");
                list.add(new VariableFormatArtifact(enabled, className, label, javaClassName));
                if ("ru.runa.wfe.var.format.ProcessIdFormat".equals(className)) {
                    continue;
                }
                if (!mappingByTypeClass.containsKey(javaClassName)) {
                    mappingByTypeClass.put(javaClassName, new ArrayList<String>());
                }
                mappingByTypeClass.get(javaClassName).add(label);
            }
        }
        filterArtifacts = Lists.newArrayList();
        filterArtifacts.add(new VariableFormatArtifact(true, null, Localization.getString("label.any"), Object.class.getName()));
        for (Map.Entry<String, List<String>> entry : mappingByTypeClass.entrySet()) {
            String label = Joiner.on(", ").join(entry.getValue());
            filterArtifacts.add(new VariableFormatArtifact(true, null, label, entry.getKey()));
        }
        Collections.sort(filterArtifacts);
    }

    public static boolean isAssignableFrom(String superClassName, String className) {
        try {
            return isAssignableFrom(Class.forName(superClassName), className);
        } catch (ClassNotFoundException e) {
            // UserType
            return false;
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(superClassName, th);
            return false;
        }
    }

    public static boolean isAssignableFrom(Class<?> superClass, String className) {
        try {
            Class<?> testingClass = Class.forName(className);
            return superClass.isAssignableFrom(testingClass);
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(className, th);
            return false;
        }
    }

    public static boolean isApplicable(Variable variable, String... classNameFilters) {
        boolean applicable = classNameFilters == null || classNameFilters.length == 0;
        if (!applicable) {
            for (String typeClassNameFilter : classNameFilters) {
                if (VariableFormatRegistry.isApplicable(variable, typeClassNameFilter)) {
                    applicable = true;
                    break;
                }
            }
        }
        return applicable;
    }

    public static boolean isApplicable(Variable variable, String classNameFilter) {
        if (variable.isComplex()) {
            if (Objects.equal(Object.class.getName(), classNameFilter)) {
                return true;
            }
            if (Objects.equal(UserTypeMap.class.getName(), classNameFilter)) {
                return true;
            }
            return Objects.equal(variable.getUserType().getName(), classNameFilter);
        }
        if (List.class.getName().equals(variable.getJavaClassName())
                && classNameFilter.contains(Variable.FORMAT_COMPONENT_TYPE_START)
                && classNameFilter.contains(Variable.FORMAT_COMPONENT_TYPE_END)) {
            String[] formatComponentClassNames = variable.getFormatComponentClassNames();
            if (formatComponentClassNames.length == 1) {
                VariableFormatArtifact variableFormatArtifact = getInstance().getArtifact(formatComponentClassNames[0]);
                if (variableFormatArtifact != null) {
                    String componentClassNameFilter = classNameFilter.substring(classNameFilter.indexOf(Variable.FORMAT_COMPONENT_TYPE_START) + 1,
                            classNameFilter.indexOf(Variable.FORMAT_COMPONENT_TYPE_END));
                    String componentClassName = variableFormatArtifact.getJavaClassName();
                    return isAssignableFrom(componentClassNameFilter, componentClassName);
                } else {
                    String userTypeClassName = variable.getJavaClassName() + Variable.FORMAT_COMPONENT_TYPE_START + formatComponentClassNames[0]
                            + Variable.FORMAT_COMPONENT_TYPE_END;
                    return Objects.equal(classNameFilter, userTypeClassName);
                }
            }
        }
        return isAssignableFrom(classNameFilter, variable.getJavaClassName());
    }

    public VariableFormatArtifact getArtifactByJavaClassName(String javaClassName) {
        for (VariableFormatArtifact artifact : getAll()) {
            if (Objects.equal(javaClassName, artifact.getJavaClassName())) {
                return artifact;
            }
        }
        return null;
    }

    public VariableFormatArtifact getArtifactNotNullByJavaClassName(String javaClassName) {
        VariableFormatArtifact artifact = getArtifactByJavaClassName(javaClassName);
        if (artifact == null) {
            throw new RuntimeException("Artifact javaClassName='" + javaClassName + "' does not exist");
        }
        return artifact;
    }

    public List<String> getSuperClassNames(String className) {
        List<String> result = Lists.newArrayList();
        for (VariableFormatArtifact artifact : getAll()) {
            if (isAssignableFrom(artifact.getJavaClassName(), className)) {
                result.add(artifact.getJavaClassName());
            }
        }
        return result;
    }

    public List<VariableFormatArtifact> getFilterArtifacts() {
        return filterArtifacts;
    }

    public String getFilterLabel(String javaClassName) {
        for (VariableFormatArtifact artifact : filterArtifacts) {
            if (Objects.equal(javaClassName, artifact.getJavaClassName())) {
                return artifact.getLabel();
            }
        }
        throw new InternalApplicationException("No filter found by type " + javaClassName);
    }

    public String getFilterJavaClassName(String label) {
        for (VariableFormatArtifact artifact : filterArtifacts) {
            if (Objects.equal(label, artifact.getLabel())) {
                return artifact.getJavaClassName();
            }
        }
        throw new InternalApplicationException("No filter found by label " + label);
    }
}

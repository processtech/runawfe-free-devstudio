package ru.runa.gpd.formeditor.ftl;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.filter.IComponentFilter;
import ru.runa.gpd.formeditor.ftl.parameter.ParameterType;
import ru.runa.gpd.formeditor.ftl.validation.IParameterTypeValidator;
import ru.runa.wfe.InternalApplicationException;

public class ComponentTypeRegistry {
    private static final Map<String, ParameterType> parameters = Maps.newHashMap();
    private static final Map<String, ComponentType> components = Maps.newHashMap();

    static {
        List<IComponentFilter> filters = Lists.newArrayList();
        try {
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("ru.runa.gpd.form.ftl.filters");
            for (IConfigurationElement element : elements) {
                try {
                    IComponentFilter filter = (IComponentFilter) element.createExecutableExtension("class");
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("Registering component filter " + filter);
                    }
                    filters.add(filter);
                } catch (Throwable th) {
                    EditorsPlugin.logError("Unable to load FTL component filter " + element, th);
                }
            }
        } catch (Throwable th) {
            EditorsPlugin.logError("Unable to load FTL component filters", th);
        }
        try {
            IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("ru.runa.gpd.form.ftl.parameters");
            for (IConfigurationElement element : elements) {
                try {
                    String id = element.getAttribute("id");
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("Registering parameter type " + id);
                    }
                    ParameterType parameterType = (ParameterType) element.createExecutableExtension("class");
                    parameterType.setDepends(element.getAttribute("depends"));
                    parameterType.setValidator((IParameterTypeValidator) element.createExecutableExtension("validator"));
                    parameters.put(id, parameterType);
                } catch (Throwable th) {
                    EditorsPlugin.logError("Unable to load FTL component parameter type " + element, th);
                }
            }
        } catch (Throwable th) {
            EditorsPlugin.logError("Unable to load FTL component parameter types", th);
        }
        try {
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.form.ftl.components").getExtensions();
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("ru.runa.gpd.form.ftl.components extensions count: " + extensions.length);
            }
            for (IExtension extension : extensions) {
                Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
                if (EditorsPlugin.DEBUG) {
                    PluginLogger.logInfo("Loading extensions from " + bundle.getSymbolicName());
                }
                IConfigurationElement[] componentElements = extension.getConfigurationElements();
                for (IConfigurationElement componentElement : componentElements) {
                    try {
                        ComponentType type = new ComponentType(bundle, componentElement);
                        IConfigurationElement[] paramElements = componentElement.getChildren();
                        for (IConfigurationElement paramElement : paramElements) {
                            ComponentParameter componentParameter = new ComponentParameter(paramElement);
                            validateComponentTypeParameterDepends(type, componentParameter);
                            type.getParameters().add(componentParameter);
                        }
                        if (EditorsPlugin.DEBUG) {
                            PluginLogger.logInfo("Registering " + type);
                        }
                        boolean register = true;
                        for (IComponentFilter filter : filters) {
                            if (filter.disable(type)) {
                                if (EditorsPlugin.DEBUG) {
                                    PluginLogger.logInfo("Filtered " + type + " by " + filter);
                                }
                                register = false;
                            }
                        }
                        if (register) {
                            components.put(type.getId(), type);
                        }
                    } catch (Throwable th) {
                        EditorsPlugin.logError("Unable to load FTL component " + componentElement, th);
                    }
                }
            }
        } catch (Throwable th) {
            EditorsPlugin.logError("Unable to load FTL components", th);
        }
    }

    public static ParameterType getParameterNotNull(String id) {
        if (!parameters.containsKey(id)) {
            throw new RuntimeException("FTL component parameter type not found by id '" + id + "'");
        }
        return parameters.get(id);
    }

    public static ComponentType getNotNull(String id) {
        if (!has(id)) {
            throw new RuntimeException("FTL component not found by id '" + id + "'");
        }
        return getAll().get(id);
    }

    public static boolean has(String id) {
        return getAll().containsKey(id);
    }

    public static Map<String, ComponentType> getAll() {
        return components;
    }

    public static List<ComponentType> getEnabled() {
        return getAll().values().stream().filter(e -> e.isEnabled()).sorted(new ComponentTypeComparator()).collect(Collectors.toList());
    }

    public static List<ComponentType> getEnabled(boolean ordered) {
        return getEnabled().stream().filter(e -> (e.getOrder() != 0) == ordered).collect(Collectors.toList());
    }

    private static final void validateComponentTypeParameterDepends(ComponentType type, ComponentParameter componentParameter)
            throws InternalApplicationException {
        final String dependsArg = componentParameter.getType().getDepends();
        if (Strings.isNullOrEmpty(dependsArg)) {
            return;
        }
        boolean dependentAdded = false;
        for (final String depends : Splitter.on(CharMatcher.is(',')).trimResults().split(dependsArg)) {
            final ParameterType dependsOnType = parameters.get(depends);
            Preconditions.checkNotNull(dependsOnType, depends);
            final Iterator<ComponentParameter> found = Iterables.filter(type.getParameters(), new Predicate<ComponentParameter>() {
                @Override
                public boolean apply(ComponentParameter param) {
                    return Objects.equal(param.getType(), dependsOnType);
                }
            }).iterator();
            if (found.hasNext()) {
                found.next().getDependents().add(componentParameter);
                dependentAdded = true;
            }
        }
        if (!dependentAdded) {
            throw new InternalApplicationException("Not found dependent component(s) " + componentParameter.getType().getDepends());
        }
    }

    private static class ComponentTypeComparator implements Comparator<ComponentType> {
        @Override
        public int compare(ComponentType t1, ComponentType t2) {
            int diff = t1.getOrder() - t2.getOrder();
            if (diff == 0) {
                return t1.getLabel().compareToIgnoreCase(t2.getLabel());
            }
            return diff;
        }
    }

}

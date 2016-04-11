package ru.runa.gpd.formeditor.ftl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.filter.IComponentFilter;
import ru.runa.gpd.formeditor.ftl.parameter.ParameterType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
                    ParameterType parameterInstance = (ParameterType) element.createExecutableExtension("class");
                    parameters.put(id, parameterInstance);
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
        List<ComponentType> tags = new ArrayList<ComponentType>();
        for (ComponentType tag : getAll().values()) {
            if (tag.isEnabled()) {
                tags.add(tag);
            }
        }
        Collections.sort(tags, new ComponentTypeComparator());
        return tags;
    }

    private static class ComponentTypeComparator implements Comparator<ComponentType> {
        @Override
        public int compare(ComponentType t1, ComponentType t2) {
            return t1.getOrder() - t2.getOrder();
        }
    }

}

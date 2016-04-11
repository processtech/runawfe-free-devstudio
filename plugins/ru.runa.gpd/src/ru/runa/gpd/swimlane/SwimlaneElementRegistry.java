package ru.runa.gpd.swimlane;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginLogger;

public class SwimlaneElementRegistry {
    private static SwimlaneElement parseElement(IConfigurationElement configElement) throws Exception {
        try {
            SwimlaneElement swimlaneElement = (SwimlaneElement) configElement.createExecutableExtension("className");
            swimlaneElement.setName(configElement.getAttribute("name"));
            swimlaneElement.setTreePath(configElement.getAttribute("treePath"));
            IConfigurationElement[] parameterElements = configElement.getChildren("param");
            for (IConfigurationElement paramElement : parameterElements) {
                String paramName = paramElement.getAttribute("name");
                String paramValue = paramElement.getAttribute("value");
                setProperty(swimlaneElement, paramName, paramValue);
            }
            IConfigurationElement[] childElements = configElement.getChildren("element");
            for (IConfigurationElement childElement : childElements) {
                SwimlaneElement child = parseElement(childElement);
                if (child != null) {
                    swimlaneElement.addChild(child);
                }
            }
            return swimlaneElement;
        } catch (Exception e) {
            PluginLogger.logError("Error processing extension 'swimlaneElements'", e);
            return null;
        }
    }

    private static void setProperty(SwimlaneElement swimlaneElement, String paramName, String paramValue) throws Exception {
        String setterMethodName = "set" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
        Method setter = swimlaneElement.getClass().getMethod(setterMethodName, new Class[] { String.class });
        setter.invoke(swimlaneElement, new Object[] { paramValue });
    }

    public static List<SwimlaneElement> getSwimlaneElements() {
        List<SwimlaneElement> swimlaneElements = new ArrayList<SwimlaneElement>();
        List<SwimlaneElement> lazySwimlaneElements = new ArrayList<SwimlaneElement>();
        try {
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.swimlaneElements").getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    SwimlaneElement swimlaneElement = parseElement(configElement);
                    if (swimlaneElement != null) {
                        if (swimlaneElement.getTreePath() == null) {
                            swimlaneElements.add(swimlaneElement);
                        } else {
                            lazySwimlaneElements.add(swimlaneElement);
                        }
                    }
                }
            }
            for (SwimlaneElement swimlaneElement : lazySwimlaneElements) {
                try {
                    String[] paths = swimlaneElement.getTreePath().split(",");
                    if (paths.length > 0) {
                        // fix for relation tab
                        paths[0] = String.valueOf(Integer.parseInt(paths[0]) + 1);
                    }
                    List<SwimlaneElement> container = swimlaneElements;
                    SwimlaneElement parent = null;
                    for (String path : paths) {
                        parent = container.get(Integer.parseInt(path));
                        container = parent.getChildren();
                    }
                    container.add(swimlaneElement);
                    swimlaneElement.setParent(parent);
                } catch (Exception e) {
                    PluginLogger.logError("Invalid TreePath in SwimlaneElement", e);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("Error processing extension 'swimlaneElements'", e);
        }
        return swimlaneElements;
    }
}

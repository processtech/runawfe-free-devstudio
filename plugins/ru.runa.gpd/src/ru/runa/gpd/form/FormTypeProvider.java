package ru.runa.gpd.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginLogger;
import ru.runa.wfe.commons.TypeConversionUtil;

public class FormTypeProvider {
    private static final String FORMTYPE_EXT_POINT_ID = "ru.runa.gpd.formtype";
    private static Map<String, FormType> formTypes = new HashMap<String, FormType>();
    private static List<FormType> sortedList = new ArrayList<FormType>();

    private static void init() {
        if (formTypes.size() == 0) {
            try {
                IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(FORMTYPE_EXT_POINT_ID).getExtensions();
                for (IExtension extension : extensions) {
                    IConfigurationElement[] configElements = extension.getConfigurationElements();
                    for (IConfigurationElement element : configElements) {
                        try {
                            FormType formType = (FormType) element.createExecutableExtension("contributor");
                            formType.setName(element.getAttribute("name"));
                            formType.setType(element.getAttribute("type"));
                            formType.setOrder(TypeConversionUtil.convertTo(int.class, element.getAttribute("order")));
                            formTypes.put(formType.getType(), formType);
                        } catch (Exception e) {
                            PluginLogger.logError("Error processing form type extension", e);
                        }
                    }
                }
                sortedList.addAll(formTypes.values());
                Collections.sort(sortedList, new FormTypeComparator());
            } catch (Exception e) {
                PluginLogger.logError("Error processing form type extension point", e);
            }
        }
    }

    public static List<FormType> getRegisteredFormTypes() {
        init();
        return sortedList;
    }

    public static FormType getFormTypeByName(String name) {
        init();
        for (FormType formType : formTypes.values()) {
            if (name.equals(formType.getName())) {
                return formType;
            }
        }
        throw new RuntimeException("No form type found, name = " + name);
    }

    public static FormType getFormType(String type) {
        init();
        if (!formTypes.containsKey(type)) {
            throw new RuntimeException("No form type found, type = " + type);
        }
        return formTypes.get(type);
    }

    private static class FormTypeComparator implements Comparator<FormType> {
        @Override
        public int compare(FormType o1, FormType o2) {
            return o2.getOrder() - o1.getOrder();
        }
    }
}

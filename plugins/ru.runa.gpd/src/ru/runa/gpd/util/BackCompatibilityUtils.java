package ru.runa.gpd.util;

import ru.runa.wfe.var.UserTypeMap;

public class BackCompatibilityUtils {
    /**
     * used from downstream plugins and for ComplexVariable workaround
     */
    public static String getClassName(java.lang.String className) {
        if ("ru.runa.wfe.var.ComplexVariable".equals(className)) {
            return UserTypeMap.class.getName();
        }
        return ru.runa.wfe.commons.BackCompatibilityClassNames.getClassName(className);
    }
}

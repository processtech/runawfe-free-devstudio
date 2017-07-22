package ru.runa.gpd;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("plugin");

    private Localization() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            System.out.println("Missed localization: '" + key + "'");
            return key;
        }
    }

    public static String getString(String key, Object... parameters) {
        String msg = getString(key);
        return MessageFormat.format(msg, parameters);
    }

}

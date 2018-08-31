package ru.runa.gpd.jointformeditor.resources;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("ru.runa.gpd.jointformeditor.resources.messages");

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    public static String getString(String key, Object... parameters) {
        String msg = getString(key);
        return MessageFormat.format(msg, parameters);
    }

}

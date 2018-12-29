package ru.runa.gpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GpdStore {

    private static final File storeFile = new File(Activator.getPreferencesFolder() + File.separator + "gpd-store.properties");
    private static final Properties data = new Properties();

    static {
        try {
            if (!storeFile.exists()) {
                storeFile.createNewFile();
            }
            data.load(new FileInputStream(storeFile));
        } catch (IOException e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage());
        }
    }

    private GpdStore() {
    }

    public static void set(String key, String value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        try {
            data.store(new FileOutputStream(storeFile), null);
        } catch (IOException e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage());
        }
    }

    public static String get(String key) {
        return data.getProperty(key);
    }

}

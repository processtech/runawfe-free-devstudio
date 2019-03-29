package ru.runa.gpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SubprocessMap {

    private static final File mapFile = new File(Activator.getPreferencesFolder() + File.separator + "subprocess-map.properties");
    private static final Properties data = new Properties();

    static {
        try {
            if (!mapFile.exists()) {
                mapFile.createNewFile();
            }
            data.load(new FileInputStream(mapFile));
        } catch (IOException e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage());
        }
    }

    private SubprocessMap() {
    }

    public static void set(String key, String value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        try {
            data.store(new FileOutputStream(mapFile), null);
        } catch (IOException e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage());
        }
    }

    public static String get(String key) {
        return data.getProperty(key);
    }

}

package ru.runa.gpd.editor.graphiti;

import java.io.File;

public class IconUtil {
    public static String getIconNameNotInterrupting(String iconName) {
        String extension = "";
        String filename = "";
        int i = iconName.lastIndexOf('.');
        if (i > 0) {
            filename = iconName.substring(iconName.indexOf(File.separator) + 1, i);
            extension = iconName.substring(i + 1);
        } else {
            filename = iconName.substring(iconName.indexOf(File.separator) + 1);
        }
        return new File(new File(iconName).getParentFile(), filename + "_notinterrupting." + extension).getPath();
    }
}

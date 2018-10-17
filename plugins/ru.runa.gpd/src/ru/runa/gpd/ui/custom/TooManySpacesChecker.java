package ru.runa.gpd.ui.custom;

public class TooManySpacesChecker {

    public static boolean isValid(String string) {
        if (string.startsWith(" ") || string.endsWith(" ") || string.contains("  ")) {
            return false;
        }
        return true;
    }

}

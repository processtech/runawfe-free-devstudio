package ru.runa.gpd.extension.decision;

import java.util.regex.Pattern;

public abstract class GroovyModel {
    public static final Pattern IF_PATTERN = Pattern.compile("if \\((.*)\\)");

    public String normalizeString(String str) {
        while (str.charAt(0) == ' ') {
            str = str.substring(1);
        }
        while (str.charAt(str.length() - 1) == ' ') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}

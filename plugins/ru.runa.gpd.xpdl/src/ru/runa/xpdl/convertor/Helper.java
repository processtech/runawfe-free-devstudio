package ru.runa.xpdl.convertor;

/**
 * Created by IntelliJ IDEA. User: mika951 Date: 31.05.12 Time: 11:06
 */
public class Helper {

    public static String generateVariableName(String nodeNameFrom) {
        return "V" + nodeNameFrom.replaceAll("[\\s\\p{Punct}]", "_");
    }
}

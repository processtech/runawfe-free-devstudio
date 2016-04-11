package ru.runa.gpd.formeditor.ftl;

public final class ComponentIdGenerator {
    private static int maxComponentId = 0;

    public static synchronized int generate() {
        return maxComponentId++;
    }

}

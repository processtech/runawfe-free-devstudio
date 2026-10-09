package ru.runa.gpd.aichat.utils;

import java.util.UUID;

public class IdGenerator {

    public static String generate(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }
}

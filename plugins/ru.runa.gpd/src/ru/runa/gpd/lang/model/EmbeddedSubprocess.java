package ru.runa.gpd.lang.model;

public class EmbeddedSubprocess extends Subprocess {
    public EmbeddedSubprocess() {
        setEmbedded(true);
    }

    public static enum Behavior {
        GraphPart,
        SeparateSubprocess
    }
}

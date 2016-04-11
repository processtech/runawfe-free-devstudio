package ru.runa.gpd.lang;

public enum Language {
    JPDL(new JpdlSerializer(), "uml"), BPMN(new BpmnSerializer(), "bpmn");
    private final ProcessSerializer serializer;
    private final String notation;

    private Language(ProcessSerializer serializer, String notation) {
        this.serializer = serializer;
        this.notation = notation;
    }

    public ProcessSerializer getSerializer() {
        return serializer;
    }

    public String getNotation() {
        return notation;
    }
}

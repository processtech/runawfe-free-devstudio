package ru.runa.gpd.aichat.sync;

public enum LlmApiType {
    RESPONSES("Responses API"),
    COMPLETIONS("Chat completion API");

    private final String name;

    LlmApiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package ru.runa.gpd.aichat.api;


public interface AiChatLlmApiStrategy {
    String sendRequest() throws Exception;

    void testConnection() throws Exception;
}

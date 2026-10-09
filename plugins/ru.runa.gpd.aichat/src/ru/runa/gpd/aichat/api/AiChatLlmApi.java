package ru.runa.gpd.aichat.api;

import ru.runa.gpd.aichat.bpmnelements.FormItem;

import java.util.List;

public class AiChatLlmApi {
    private AiChatLlmApiStrategy strategy;

    public void setStrategy(AiChatLlmApiStrategy strategy) {
        this.strategy = strategy;
    }

    public String sendRequest() throws Exception {
        return strategy.sendRequest();
    }

    public void testConnection() throws Exception {
        strategy.testConnection();
    }
}

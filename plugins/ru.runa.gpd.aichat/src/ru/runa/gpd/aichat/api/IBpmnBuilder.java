package ru.runa.gpd.aichat.api;

import com.fasterxml.jackson.databind.JsonNode;

public interface IBpmnBuilder {
    void newBpmn(JsonNode params) throws Exception;
    void openBpmn(JsonNode params) throws Exception;
    void addEvent(JsonNode params) throws Exception;
    void addActivity(JsonNode params) throws Exception;
    void addGateway(JsonNode params) throws Exception;
    void connect(JsonNode params) throws Exception;
    void deleteConnect(JsonNode params) throws Exception;
    void addLane(JsonNode params) throws Exception;
    void addVariable(JsonNode params) throws Exception;
    void deleteElement(JsonNode params) throws Exception;
    String getVariablesXml() throws Exception;
    void autoLayout() throws Exception;
    String exportXml() throws Exception;
}

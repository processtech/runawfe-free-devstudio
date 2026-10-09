package ru.runa.gpd.aichat.settings;

public interface PrefConstants {
    String P_LLM_CONNECTOR_INDICES = "llmConnectorIndices";
    String P_LLM_CONNECTOR_SELECTED_INDEX = "llmConnectorSelectedIndex";
    String P_LLM_CONNECTOR_HOST_SUFFIX = "host";
    String P_LLM_CONNECTOR_MODEL_NAME_SUFFIX = "modelName";
    String P_LLM_CONNECTOR_API_KEY_SUFFIX = "apiKey";
    String P_LLM_CONNECTOR_PROTOCOL_SUFFIX = "protocol";
    String P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX = "allowSslInsecure";
    String P_LLM_CONNECTOR_API_TYPE_SUFFIX = "apiType";
    String P_LLM_SYSTEM_PROMPT_KEY = "llm.prompts.systemPrompt.default.text";
    String P_LLM_ADDITIONAL_RULES_PROMPT_KEY = "llm.prompts.additionalRules.default.text";
}

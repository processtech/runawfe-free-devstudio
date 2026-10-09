package ru.runa.gpd.aichat.sync;

import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.aichat.settings.AiChatLlmConnectorPreferenceNode;
import ru.runa.gpd.aichat.settings.AiChatLlmConnectorsPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.aichat.Activator;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_API_KEY_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_API_TYPE_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_HOST_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_MODEL_NAME_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_PROTOCOL_SUFFIX;

public class AiChatLlmConnectorSettings implements PrefConstants {
    private final int index;
    private final String protocol;
    private final boolean allowSslInsecure;
    private final String host;
    private final String modelName;
    private final String apiKey;
    private String version;
    private String apiType;

    public static AiChatLlmConnectorSettings load(int index) {
        return new AiChatLlmConnectorSettings(index, true);
    }

    public static AiChatLlmConnectorSettings loadSelected() {
        return new AiChatLlmConnectorSettings(AiChatLlmConnectorsPreferenceNode.getSelectedIndex(), true);
    }

    public static AiChatLlmConnectorSettings createDefault(int index) {
        return new AiChatLlmConnectorSettings(index, false);
    }

    private AiChatLlmConnectorSettings(int index, boolean loadFromStore) {
        this.index = index;
        if (loadFromStore) {
            String prefix = AiChatLlmConnectorPreferenceNode.getId(index);
            this.protocol = Activator.getPrefString(prefix + '.' + P_LLM_CONNECTOR_PROTOCOL_SUFFIX);
            this.allowSslInsecure = Activator.getPrefBoolean(prefix + '.' + P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX);
            this.host = Activator.getPrefString(prefix + '.' + P_LLM_CONNECTOR_HOST_SUFFIX);
            //this.port = Activator.getPrefInt(prefix + '.' + P_LLM_CONNECTOR_PORT_SUFFIX);
            this.modelName = Activator.getPrefString(prefix + '.' + P_LLM_CONNECTOR_MODEL_NAME_SUFFIX);
            this.apiKey = Activator.getPrefString(prefix + '.' + P_LLM_CONNECTOR_API_KEY_SUFFIX);
            this.apiType = Activator.getPrefString(prefix + '.' + P_LLM_CONNECTOR_API_TYPE_SUFFIX);
        } else {
            this.protocol = "http";
            this.allowSslInsecure = false;
            this.host = System.getProperty("wfe.default.host", "localhost");
            this.modelName = "";
            this.apiKey = "";
            this.apiType = "completions";
        }
    }

    public String getApiType() {
        return this.apiType;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isAllowSslInsecure() {
        return allowSslInsecure;
    }

    public String getHost() {
        return host;
    }

    public String getVersion() {
        return version;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return protocol + "://" + host;
    }

    public void saveToStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = AiChatLlmConnectorPreferenceNode.getId(index);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_PROTOCOL_SUFFIX, protocol);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX, allowSslInsecure);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_HOST_SUFFIX, host);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_MODEL_NAME_SUFFIX, modelName);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_API_KEY_SUFFIX, apiKey);
        store.setValue(prefix + "." + P_LLM_CONNECTOR_API_TYPE_SUFFIX, apiType);
    }

    public void saveDefaultToStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = AiChatLlmConnectorPreferenceNode.getId(index);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_PROTOCOL_SUFFIX, protocol);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX, allowSslInsecure);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_HOST_SUFFIX, host);
        //store.setDefault(prefix + "." + P_LLM_CONNECTOR_PORT_SUFFIX, port);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_MODEL_NAME_SUFFIX, modelName);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_API_KEY_SUFFIX, apiKey);
        store.setDefault(prefix + "." + P_LLM_CONNECTOR_API_TYPE_SUFFIX, apiType);
    }

    public void removeFromStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = AiChatLlmConnectorPreferenceNode.getId(index);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_PROTOCOL_SUFFIX);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_HOST_SUFFIX);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_MODEL_NAME_SUFFIX);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_API_KEY_SUFFIX);
        store.setToDefault(prefix + "." + P_LLM_CONNECTOR_API_TYPE_SUFFIX);
    }
}

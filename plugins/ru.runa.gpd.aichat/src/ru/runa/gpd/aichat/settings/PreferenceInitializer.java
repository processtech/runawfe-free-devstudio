package ru.runa.gpd.aichat.settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.aichat.sync.AiChatLlmConnectorSettings;

public class PreferenceInitializer extends AbstractPreferenceInitializer implements PrefConstants {
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(P_LLM_CONNECTOR_INDICES, "0");
        store.setDefault(P_LLM_CONNECTOR_SELECTED_INDEX, 0);
        try {
            store.setDefault(P_LLM_SYSTEM_PROMPT_KEY, AiChatPromptsPreferencePage.getSystemPromptDefault());
            store.setDefault(P_LLM_ADDITIONAL_RULES_PROMPT_KEY, AiChatPromptsPreferencePage.getAdditionalRulesPromptDefault());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AiChatLlmConnectorSettings llmSettings = AiChatLlmConnectorSettings.createDefault(0);
        llmSettings.saveDefaultToStore();
    }
}

package ru.runa.gpd.settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.EmbeddedSubprocess.Behavior;
import ru.runa.gpd.sync.WfeServerConnectorSettings;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements PrefConstants {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(P_BPMN_SHOW_SWIMLANE, true);
        store.setDefault(P_DEFAULT_LANGUAGE, Language.BPMN.toString());
        store.setDefault(P_FORM_WEB_SERVER_PORT, "48780");
        store.setDefault(P_FORM_EXTERNAL_EDITOR_PATH, "");
        store.setDefault(P_FORM_USE_EXTERNAL_EDITOR, false);
        store.setDefault(P_FORM_IGNORE_ERRORS_FROM_WEBPAGE, true);

        store.setDefault(P_WFE_SERVER_CONNECTOR_INDICES, "0");
        store.setDefault(P_WFE_SERVER_CONNECTOR_SELECTED_INDEX, 0);
        WfeServerConnectorSettings connectorSettings = WfeServerConnectorSettings.createDefault(0);
        connectorSettings.saveDefaultToStore();

        store.setDefault(P_LDAP_CONNECTION_PROVIDER_URL, "ldap://192.168.0.1/dc=domain,dc=com");
        store.setDefault(P_DATE_FORMAT_PATTERN, "dd.MM.yyyy");
        store.setDefault(P_ENABLE_REGULATIONS_MENU_ITEMS, Localization.getString("disable"));
        store.setDefault(P_ENABLE_EXPORT_WITH_SCALING, Localization.getString("disable"));
        store.setDefault(P_ENABLE_EDITING_COMMENT_HISTORY_XML, Localization.getString("disable"));
        store.setDefault(P_CONFIRM_DELETION, true);
        store.setDefault(P_PROCESS_SAVE_HISTORY, true);
        store.setDefault(P_PROCESS_SAVEPOINT_NUMBER, 10);
        store.setDefault(P_ENABLE_USER_ACTIVITY_LOGGING, true);
        store.setDefault(P_KEEP_VARIABLE_VALIDATION_ON_COMPONENT_REMOVAL, false);
        store.setDefault(P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED, true);
        store.setDefault(P_GLOBAL_OBJECTS_ENABLED, true);
        // PreferenceConverter.FONTDATA_DEFAULT_DEFAULT
        // backward compatibility
        store.setDefault(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT), new FontData("Arial", 8, SWT.NORMAL).toString());
        store.setDefault(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT_COLOR), "100, 100, 100");
        store.setDefault(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_BACKGROUND_COLOR), "250, 251, 252");
        store.setDefault(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FOREGROUND_COLOR), "3, 104, 154");
        store.setDefault(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_LINE_WIDTH), 2);
        store.setDefault(P_BPMN_EXPAND_CONTEXT_BUTTON_PAD, true);
        store.setDefault(LanguageElementPreferenceNode.getBpmnPropertyName(StyleUtil.TEXT_ANNOTATION_BPMN_NAME, P_BPMN_FOREGROUND_COLOR), "0, 0, 0");
        store.setDefault(LanguageElementPreferenceNode.getBpmnPropertyName(StyleUtil.TRANSITION_BPMN_NAME, P_BPMN_FOREGROUND_COLOR), "0, 0, 0");
        store.setDefault(LanguageElementPreferenceNode.getBpmnPropertyName(StyleUtil.TRANSITION_BPMN_NAME, P_BPMN_LINE_WIDTH), 1);
        store.setDefault(LanguageElementPreferenceNode.getBpmnPropertyName("exclusiveGateway", P_BPMN_MARK_DEFAULT_TRANSITION), true);
        store.setDefault(P_ENABLE_USE_BOT_CONFIG_WITHOUT_PARAMETERS_OPTION, false);
        store.setDefault(P_SHOW_XML_BOT_CONFIG, false);
        store.setDefault(P_DISABLE_DOCX_TEMPLATE_VALIDATION, false);
        store.setDefault(P_EMBEDDED_SUBPROCESS_BEHAVIOR, Behavior.GraphPart.name());
        store.setDefault(P_PROPERTIES_VIEW_ID, PROPERTIES_VIEW_LEGACY);
    }
}

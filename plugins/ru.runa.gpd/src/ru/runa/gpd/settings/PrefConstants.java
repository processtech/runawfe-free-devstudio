package ru.runa.gpd.settings;

/**
 * Constant definitions for plug-in preferences
 */
public interface PrefConstants {
    public static final String AUTHENTICATION_TYPE_LOGIN_PASSWORD = "authType.login_password";
    public static final String AUTHENTICATION_TYPE_KERBEROS = "authType.kerberos";
    public static final String P_BPMN_SHOW_SWIMLANE = "showSwimlane";
    public static final String P_BPMN_FONT = "font";
    public static final String P_BPMN_FONT_COLOR = "fontColor";
    public static final String P_BPMN_BACKGROUND_COLOR = "backgroundColor";
    public static final String P_BPMN_FOREGROUND_COLOR = "foregroundColor";
    public static final String P_BPMN_LINE_WIDTH = "lineWidth";
    public static final String P_BPMN_MARK_DEFAULT_TRANSITION = "markDefaultTransition";
    public static final String P_BPMN_DEFAULT_TRANSITION_NAMES = "defaultTransitionNames";
    public static final String P_BPMN_EXPAND_CONTEXT_BUTTON_PAD = "expandContextButtonPad";
    public static final String P_LANGUAGE_NODE_NAME_PATTERN = "nodeNamePattern";
    public static final String P_LANGUAGE_NODE_WIDTH = "nodeWidth";
    public static final String P_LANGUAGE_NODE_HEIGHT = "nodeHeight";
    public static final String P_LANGUAGE_SWIMLANE_INITIALIZER = "reassignSwimlaneToInitializer";
    public static final String P_LANGUAGE_SWIMLANE_PERFORMER = "reassignSwimlaneToTaskPerformer";
    public static final String P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA = "inputDataAllowedInAsyncTaskNode";
    public static final String P_LANGUAGE_SUB_PROCESS_ASYNC_INPUT_DATA = "inputDataAllowedInAsyncSubprocess";
    public static final String P_FORM_WEB_SERVER_PORT = "editorWebPort";
    public static final String P_FORM_USE_EXTERNAL_EDITOR = "useExternalEditor";
    public static final String P_FORM_EXTERNAL_EDITOR_PATH = "externalEditorPath";
    public static final String P_FORM_IGNORE_ERRORS_FROM_WEBPAGE = "ignoreErrorsFromWebPage";
    public static final String P_JOINT_FORM_EDITOR_SELECTED_PAGE = "jointEditorSelectedPage";
    public static final String P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM = "form";
    public static final String P_JOINT_FORM_EDITOR_SELECTED_PAGE_SCRIPT = "script";
    public static final String P_JOINT_FORM_EDITOR_SELECTED_PAGE_VALIDATION = "validation";
    public static final String P_ESCALATION_DURATION = "escalationDuration";
    public static final String P_ESCALATION_CONFIG = "escalationConfig";
    public static final String P_ESCALATION_REPEAT = "escalationRepeat";
    public static final String P_WFE_SERVER_CONNECTOR_INDICES = "wfeServerConnectorIndices";
    public static final String P_WFE_SERVER_CONNECTOR_SELECTED_INDEX = "wfeServerConnectorSelectedIndex";
    public static final String P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX = "login";
    public static final String P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX = "password";
    public static final String P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX = "authenticationType";
    public static final String P_WFE_SERVER_CONNECTOR_HOST_SUFFIX = "host";
    public static final String P_WFE_SERVER_CONNECTOR_PORT_SUFFIX = "port";
    public static final String P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX = "protocol";
    public static final String P_WFE_SERVER_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX = "allowSslInsecure";
    public static final String P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX = "loadProcessDefinitionsHistory";
    public static final String P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX = "allowUpdateLastVersionByKeyBinding";
    public static final String P_LDAP_CONNECTION_LOGIN = "ldapLogin";
    public static final String P_LDAP_CONNECTION_PASSWORD = "ldapPassword";
    public static final String P_LDAP_CONNECTION_LOGIN_MODE = "ldapLoginMode";
    public static final String P_LDAP_CONNECTION_PROVIDER_URL = "ldapServerUrl";
    public static final String P_LDAP_CONNECTION_OU = "ldapOu";
    public static final String P_LDAP_CONNECTION_DC = "ldapDc";
    public static final String P_DEFAULT_LANGUAGE = "defaultLanguage";
    public static final String P_DATE_FORMAT_PATTERN = "dateFormat";
    public static final String P_ENABLE_REGULATIONS_MENU_ITEMS = "enabledRegulationsMenuItems";
    public static final String P_ENABLE_EXPORT_WITH_SCALING = "enabledExportWithScaling";
    public static final String P_ENABLE_EDITING_COMMENT_HISTORY_XML = "enabledEditingCommentHistoryXml";
    public static final String P_CONFIRM_DELETION = "confirmDeletion";
    public static final String P_PROCESS_SAVE_HISTORY = "processSaveHistory";
    public static final String P_PROCESS_SAVEPOINT_NUMBER = "processSavepointNumber";
    public static final String P_ENABLE_USER_ACTIVITY_LOGGING = "enableUserActivityLogging";
    public static final String P_KEEP_VARIABLE_VALIDATION_ON_COMPONENT_REMOVAL = "keepVariableValidationOnComponentRemoval";
    public static final String P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED = "internalStorageFunctionalityEnabled";
    public static final String P_GLOBAL_OBJECTS_ENABLED = "globalObjectsEnabled";
    public static final String P_CHAT_FUNCTIONALITY_ENABLED = "chatFunctionalityEnabled";
    public static final String P_ENABLE_USE_BOT_CONFIG_WITHOUT_PARAMETERS_OPTION = "useBotConfigurationWithoutParametersOption";
    public static final String P_SHOW_XML_BOT_CONFIG = "showXmlBotConfig";
    public static final String P_DISABLE_DOCX_TEMPLATE_VALIDATION = "disableDocxTemplateValidation";
}

package ru.runa.gpd.settings;

/**
 * Constant definitions for plug-in preferences
 */
public interface PrefConstants {
    public static final String LOGIN_MODE_LOGIN_PASSWORD = "login.mode.login_password";
    public static final String LOGIN_MODE_KERBEROS = "login.mode.kerberos";
    public static final String P_BPMN_SHOW_SWIMLANE = "showSwimlane";
    public static final String P_BPMN_FONT = "bpmnFont";
    public static final String P_BPMN_COLOR_FONT = "bpmnColorFont";
    public static final String P_BPMN_COLOR_BACKGROUND = "bpmnColorBackground";
    public static final String P_BPMN_COLOR_BASE = "bpmnColorBase";
    public static final String P_BPMN_COLOR_TRANSITION = "bpmnColorTransition";
    
    public static final String P_BPMN_STATE_FONT = "bpmnStateFont";
    public static final String P_BPMN_STATE_FONT_COLOR = "bpmnStateFontColor";
    public static final String P_BPMN_STATE_BACKGROUND_COLOR = "bpmnStateBackgroundColor";
    public static final String P_BPMN_STATE_BASE_COLOR = "bpmnStateBaseColor";
    
    public static final String P_BPMN_ENDTOKEN_FONT = "bpmnEndTokenFont";
    public static final String P_BPMN_ENDTOKEN_FONT_COLOR = "bpmnEndTokenFontColor";
    
    public static final String P_BPMN_SCRIPTTASK_FONT = "bpmnScriptTaskFont";
    public static final String P_BPMN_SCRIPTTASK_FONT_COLOR = "bpmnScriptTaskFontColor";
    public static final String P_BPMN_SCRIPTTASK_BACKGROUND_COLOR = "bpmnScriptTaskBackgroundColor";
    public static final String P_BPMN_SCRIPTTASK_BASE_COLOR = "bpmnScriptTaskBaseColor";
    
    public static final String P_BPMN_MULTITASKSTATE_FONT = "bpmnMultiTaskStateFont";
    public static final String P_BPMN_MULTITASKSTATE_FONT_COLOR = "bpmnMultiTaskStateFontColor";
    public static final String P_BPMN_MULTITASKSTATE_BACKGROUND_COLOR = "bpmnMultiTaskStateBackgroundColor";
    public static final String P_BPMN_MULTITASKSTATE_BASE_COLOR = "bpmnMultiTaskStateBaseColor";
    
    public static final String P_BPMN_MULTISUBPROCESS_FONT = "bpmnMultiSubprocessFont";
    public static final String P_BPMN_MULTISUBPROCESS_FONT_COLOR = "bpmnMultiSubprocessFontColor";
    public static final String P_BPMN_MULTISUBPROCESS_BACKGROUND_COLOR = "bpmnMultiSubprocessBackgroundColor";
    public static final String P_BPMN_MULTISUBPROCESS_BASE_COLOR = "bpmnMultiSubprocessBaseColor";
    
    public static final String P_BPMN_STARTSTATE_FONT = "bpmnStartStateFont";
    public static final String P_BPMN_STARTSTATE_FONT_COLOR = "bpmnStartStateFontColor";

    public static final String P_BPMN_END_FONT = "bpmnEndFont";
    public static final String P_BPMN_END_FONT_COLOR = "bpmnEndFontColor";
    
    public static final String P_BPMN_SUBPROCESS_FONT = "bpmnSubprocessFont";
    public static final String P_BPMN_SUBPROCESS_FONT_COLOR = "bpmnSubprocessFontColor";
    public static final String P_BPMN_SUBPROCESS_BACKGROUND_COLOR = "bpmnSubprocessBackgroundColor";
    public static final String P_BPMN_SUBPROCESS_BASE_COLOR = "bpmnSubprocessBaseColor";
    public static final String P_LANGUAGE_NODE_NAME_PATTERN = "nodeNamePattern";
    public static final String P_LANGUAGE_NODE_WIDTH = "nodeWidth";
    public static final String P_LANGUAGE_NODE_HEIGHT = "nodeHeight";
    public static final String P_LANGUAGE_SWIMLANE_INITIALIZER = "reassignSwimlaneToInitializerValue";
    public static final String P_LANGUAGE_SWIMLANE_PERFORMER = "reassignSwimlaneToTaskPerformer";
    public static final String P_LANGUAGE_TASK_STATE_ASYNC_INPUT_DATA = "inputDataAllowedInAsyncTaskNode";
    public static final String P_LANGUAGE_SUB_PROCESS_ASYNC_INPUT_DATA = "inputDataAllowedInAsyncSubprocess";
    public static final String P_FORM_DEFAULT_FCK_EDITOR = "defaultFCKEditor";
    public static final String FORM_FCK_EDITOR = "fck2";
    public static final String FORM_CK_EDITOR4 = "ck4";
    public static final String P_FORM_WEB_SERVER_PORT = "editorWebPort";
    public static final String P_FORM_USE_EXTERNAL_EDITOR = "useExternalEditor";
    public static final String P_FORM_EXTERNAL_EDITOR_PATH = "externalEditorPath";
    // public static final String P_TASKS_TIMEOUT_ENABLED = "useTasksTimeout";
    public static final String P_ESCALATION_DURATION = "escalationDuration";
    // public static final String P_TASKS_TIMEOUT_ACTION_CLASS =
    // "tasksTimeoutActionClass";
    public static final String P_ESCALATION_CONFIG = "escalationConfig";
    public static final String P_ESCALATION_REPEAT = "escalationRepeat";
    public static final String P_WFE_CONNECTION_TYPE = "wfeConnectorType";
    public static final String P_WFE_CONNECTION_LOGIN = "wfeLogin";
    public static final String P_WFE_CONNECTION_PASSWORD = "wfePassword";
    public static final String P_WFE_CONNECTION_LOGIN_MODE = "wfeLoginMode";
    public static final String P_WFE_CONNECTION_HOST = "wfeServerHost";
    public static final String P_WFE_CONNECTION_PORT = "wfeServerPort";
    public static final String P_WFE_CONNECTION_VERSION = "wfeServerVersion";
    public static final String P_WFE_CONNECTION_INITIAL_CTX_FACTORY = "wfeInitialCtxFactory";
    public static final String P_WFE_CONNECTION_URL_PKG_PREFIXES = "wfeUrlPkg";
    public static final String P_WFE_LOAD_PROCESS_DEFINITIONS_HISTORY = "wfeLoadProcessDefinitionsHistory";
    public static final String P_LDAP_CONNECTION_LOGIN = "ldapLogin";
    public static final String P_LDAP_CONNECTION_PASSWORD = "ldapPassword";
    public static final String P_LDAP_CONNECTION_LOGIN_MODE = "ldapLoginMode";
    public static final String P_LDAP_CONNECTION_PROVIDER_URL = "ldapServerUrl";
    public static final String P_LDAP_CONNECTION_OU = "ldapOu";
    public static final String P_LDAP_CONNECTION_DC = "ldapDc";
    public static final String P_DEFAULT_LANGUAGE = "defaultLanguage";
    public static final String P_DATE_FORMAT_PATTERN = "dateFormat";
    public static final String P_ENABLE_REGULATIONS_MENU_ITEMS = "enabledRegulationsMenuItems";
}

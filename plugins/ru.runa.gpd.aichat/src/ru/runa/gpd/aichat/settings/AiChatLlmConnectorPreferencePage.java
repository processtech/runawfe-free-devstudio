package ru.runa.gpd.aichat.settings;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.aichat.Messages;
import ru.runa.gpd.aichat.api.AiChatLlmApi;
import ru.runa.gpd.aichat.api.AiChatLlmApiCompletionsStrategy;
import ru.runa.gpd.aichat.api.AiChatLlmApiResponsesStrategy;
import ru.runa.gpd.aichat.sync.LlmApiType;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.aichat.sync.AiChatLlmConnectorSettings;
import ru.runa.gpd.ui.custom.Dialogs;

import java.util.Objects;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_API_KEY_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_API_TYPE_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_HOST_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_MODEL_NAME_SUFFIX;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_PROTOCOL_SUFFIX;


public class AiChatLlmConnectorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String SLASH = "/";
    private static final String CHAT_COMPLETIONS = "/chat/completions";
    private final String id;
    private Combo protocolCombo;
    private Combo apiTypeCombo;
    private StringFieldEditor hostEditor;
    private ComboFieldEditor protocolEditor;
    private ComboFieldEditor apiTypeEditor;
    private Text hostText;
    private Text modelName;
    private StringFieldEditor modelNameEditor;
    private StringFieldEditor apiKeyEditor;
    private Text apiKeyText;
    private Button testButton;
    private Button deleteButton;

    public AiChatLlmConnectorPreferencePage(String id) {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Messages.getString("pref.aichat.connection.llm.title"));
        this.id = id;
        noDefaultButton();
    }

    @Override
    protected void createFieldEditors() {
        protocolEditor = new ComboFieldEditor(getKey(P_LLM_CONNECTOR_PROTOCOL_SUFFIX), Messages.getString("pref.aichat.connection.llm.protocol"),
                getProtocolEntriesArray(), getFieldEditorParent()) {

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                for (Control child : parent.getChildren()) {
                    if (child instanceof Combo) {
                        protocolCombo = (Combo) child;
                        break;
                    }
                }
            }
        };
        addField(protocolEditor);

        addField(new BooleanFieldEditor(getKey(P_LLM_CONNECTOR_ALLOW_SSL_INSECURE_SUFFIX),
                Messages.getString("pref.aichat.connection.llm.ssl.allow.insecure"), getFieldEditorParent()));

        hostEditor = new StringFieldEditor(getKey(P_LLM_CONNECTOR_HOST_SUFFIX), Messages.getString("pref.aichat.connection.llm.host"),
                getFieldEditorParent()) {

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                for (Control child : parent.getChildren()) {
                    if (child instanceof Text) {
                        hostText = (Text) child;
                        break;
                    }
                }
            }
        };
        addField(hostEditor);

        apiTypeEditor = new ComboFieldEditor(getKey(P_LLM_CONNECTOR_API_TYPE_SUFFIX), Messages.getString("pref.aichat.connection.llm.apiType"),
                getApiTypeEntriesArray(), getFieldEditorParent()) {

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                for (Control child : parent.getChildren()) {
                    if (child instanceof Combo) {
                        apiTypeCombo = (Combo) child;
                        break;
                    }
                }
            }
        };
        addField(apiTypeEditor);

        modelNameEditor = new StringFieldEditor(getKey(P_LLM_CONNECTOR_MODEL_NAME_SUFFIX), Messages.getString("pref.aichat.connection.llm.modelName"),
                getFieldEditorParent()) {

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                for (Control child : parent.getChildren()) {
                    if (child instanceof Text) {
                        modelName = (Text) child;
                        break;
                    }
                }
            }
        };
        addField(modelNameEditor);

        apiKeyEditor = new StringFieldEditor(getKey(P_LLM_CONNECTOR_API_KEY_SUFFIX), Messages.getString("pref.aichat.connection.llm.apiKey"),
                getFieldEditorParent()) {

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                for (Control child : parent.getChildren()) {
                    if (child instanceof Text) {
                        apiKeyText = (Text) child;
                        break;
                    }
                }
            }
        };
        addField(apiKeyEditor);
    }

    private String getKey(String property) {
        return id + '.' + property;
    }

    private static String[][] getApiTypeEntriesArray() {
        return new String[][] {
                { LlmApiType.COMPLETIONS.getName(), LlmApiType.COMPLETIONS.getName() },
                //{ LlmApiType.RESPONSES.getName(), LlmApiType.RESPONSES.getName() }
        };
    }

    private static String[][] getProtocolEntriesArray() {
        return new String[][] {
                {HTTP, HTTP},
                {HTTPS, HTTPS}
        };
    }

    @Override
    public void init(IWorkbench iWorkbench) {

    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        testButton = new Button(buttonBar, SWT.PUSH);
        testButton.setText(Localization.getString("button.test.connection"));
        Dialog.applyDialogFont(testButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        testButton.setLayoutData(data);
        testButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    performApply();
                    AiChatLlmConnectorsPreferenceNode llmConnectorsNode = AiChatLlmConnectorsPreferenceNode.getInstance();
                    AiChatLlmConnectorPreferenceNode node = (AiChatLlmConnectorPreferenceNode) llmConnectorsNode.findSubNode(id);
                    AiChatLlmConnectorSettings settings = AiChatLlmConnectorSettings.load(node.getIndex());
                    AiChatLlmApi api = new AiChatLlmApi();
                    if (Objects.equals(settings.getApiType(), LlmApiType.COMPLETIONS.getName())) {
                        api.setStrategy(new AiChatLlmApiCompletionsStrategy(settings));
                    } else if (Objects.equals(settings.getApiType(), LlmApiType.RESPONSES.getName())) {
                        api.setStrategy(new AiChatLlmApiResponsesStrategy());
                    }
                    api.testConnection();
                    Dialogs.information(Localization.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.ConnectionFailed"), th);
                }
            }
        });
        testButton.setEnabled(isValid());
        applyDialogFont(buttonBar);

        ((GridLayout) buttonBar.getLayout()).numColumns++;
        deleteButton = new Button(buttonBar, SWT.PUSH);
        deleteButton.setText(Localization.getString("button.delete.connection"));
        Dialog.applyDialogFont(deleteButton);
        deleteButton.setLayoutData(data);
        deleteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (!id.equals(AiChatLlmConnectorsPreferenceNode.getSelectedPrefix())) {
                        AiChatLlmConnectorsPreferenceNode llmConnectorsNode = AiChatLlmConnectorsPreferenceNode.getInstance();
                        IPreferenceNode node = llmConnectorsNode.findSubNode(id);
                        llmConnectorsNode.remove(node);
                        llmConnectorsNode.updateUi(getContainer(), AiChatLlmConnectorsPreferenceNode.getSelectedPrefix());
                    } else {
                        Dialogs.warning(Localization.getString("warn.DeleteConnection"));
                    }
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error"), th);
                }
            }

        });
        deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        deleteButton.setEnabled(isValid());
        applyDialogFont(buttonBar);
    }

    private FocusListener hostFocusListener = new FocusListener() {

        @Override
        public void focusLost(FocusEvent e) {
            adjustHost();
        }

        @Override
        public void focusGained(FocusEvent e) {
            adjustHost();
        }

        private void adjustHost() {
            String protocol = getProtocolFromHost(hostText.getText());
            if (Objects.equals(protocol, HTTP)) {
                protocolCombo.select(0);
                protocolCombo.notifyListeners(SWT.Selection, new Event());
            } else if (Objects.equals(protocol, HTTPS)) {
                protocolCombo.select(1);
                protocolCombo.notifyListeners(SWT.Selection, new Event());
            }

            String host = hostWithoutProtocol(hostText.getText());
            host = hostWithoutChatCompletions(host);
            hostText.setText(host);
        }
    };

    private String getProtocolFromHost(String url) {
        if (url.startsWith(HTTPS)) {
            return HTTPS;
        } else if (url.startsWith(HTTP)) {
            return HTTP;
        }
        return null;
    }

    private String hostWithoutProtocol(String url) {
        if (!url.startsWith(HTTP)) {
            return url;
        }
        int colonIndex = url.indexOf(':');
        if (colonIndex >= 0) {
            url = url.substring(colonIndex + 1);
            while (url.startsWith(SLASH)) {
                url = url.substring(1);
            }
        }
        return url;
    }

    private String hostWithoutChatCompletions(String url) {
        if (url.endsWith(CHAT_COMPLETIONS)) {
            url = url.replace(CHAT_COMPLETIONS, "");
        } else if (url.endsWith(CHAT_COMPLETIONS + "/")) {
            url = url.replace(CHAT_COMPLETIONS + "/", "");
        }
        return url;
    }

    @Override
    protected void initialize() {
        super.initialize();
        hostText.addFocusListener(hostFocusListener);
    }

    @Override
    public void dispose() {
        hostText.removeFocusListener(hostFocusListener);
        super.dispose();
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        testButton.setEnabled(isValid());
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        AiChatLlmConnectorsPreferenceNode llmConnectorsNode = AiChatLlmConnectorsPreferenceNode.getInstance();
        AiChatLlmConnectorPreferenceNode node = (AiChatLlmConnectorPreferenceNode) llmConnectorsNode.findSubNode(id);
        node.updateName();
        llmConnectorsNode.updateUi(getContainer(), node.getId());
        return result;
    }
}

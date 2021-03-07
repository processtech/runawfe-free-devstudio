package ru.runa.gpd.settings;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorSettings;
import ru.runa.gpd.ui.custom.Dialogs;

public class WfeServerConnectorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private static final String SLASH = "/";
    private static final String HTTP = "http";
    private static final String HTTP_DEFAULT_PORT = "8080";
    private static final String HTTPS = "https";
    private static final String HTTPS_DEFAULT_PORT = "8443";
    private static final String LOCALHOST = "localhost";

    private final String id;
    private Combo protocolCombo;
    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private StringFieldEditor hostEditor;
    private Text hostText;
    private StringFieldEditor portEditor;
    private Button testButton;
    private Button deleteButton;

    public WfeServerConnectorPreferencePage(String id) {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
        this.id = id;
        noDefaultButton();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    private static String[][] getProtocolEntriesArray() {
        String[][] strings = new String[2][2];
        strings[0][0] = HTTP;
        strings[0][1] = HTTP;
        strings[1][0] = HTTPS;
        strings[1][1] = HTTPS;
        return strings;
    }

    @Override
    public void createFieldEditors() {
        addField(new ComboFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX), Localization.getString("pref.connection.wfe.protocol"),
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

            @Override
            protected void doStore() {
                super.doStore();
                getPreferenceStore().setValue(getPreferenceName(), protocolCombo.getText());
            }
        });
        hostEditor = new StringFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_HOST_SUFFIX), Localization.getString("pref.connection.wfe.host"),
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
        portEditor = new IntegerFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_PORT_SUFFIX), Localization.getString("pref.connection.wfe.port"),
                getFieldEditorParent());
        addField(portEditor);
        addField(
                new RadioGroupFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX),
                        Localization.getString("pref.connection.loginMode"), 2,
                        new String[][] { { Localization.getString("pref.connection.loginMode.byLogin"), AUTHENTICATION_TYPE_LOGIN_PASSWORD },
                                { Localization.getString("pref.connection.loginMode.byKerberos"), AUTHENTICATION_TYPE_KERBEROS } },
                        getFieldEditorParent()));
        loginEditor = new StringFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX), Localization.getString("pref.connection.login"),
                getFieldEditorParent());
        passwordEditor = new StringFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX), Localization.getString("pref.connection.password"),
                getFieldEditorParent());
        passwordEditor.setEmptyStringAllowed(true);
        passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
        boolean enabled = AUTHENTICATION_TYPE_LOGIN_PASSWORD
                .equals(Activator.getPrefString(getKey(P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX)));
        loginEditor.setEnabled(enabled, getFieldEditorParent());
        passwordEditor.setEnabled(enabled, getFieldEditorParent());
        addField(loginEditor);
        addField(passwordEditor);
        addField(new BooleanFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX),
                Localization.getString("pref.connection.wfe.load.process.definitions.history"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX),
                Localization.getString("pref.connection.allow.update.keybinding"), getFieldEditorParent()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (getKey(P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX).equals(fieldEditor.getPreferenceName())) {
                boolean enabled = AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(event.getNewValue());
                loginEditor.setEnabled(enabled, getFieldEditorParent());
                passwordEditor.setEnabled(enabled, getFieldEditorParent());
            }
            else if (getKey(P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX).equals(fieldEditor.getPreferenceName())) {
                String newProtocol = ((String) event.getNewValue()).toLowerCase();
                String oldPort = portEditor.getStringValue();
                if (HTTP.equals(newProtocol)) {
                    if (HTTPS_DEFAULT_PORT.equals(oldPort)) {
                        portEditor.setStringValue(HTTP_DEFAULT_PORT);
                    }
                } else if (HTTP_DEFAULT_PORT.equals(oldPort)) {
                    portEditor.setStringValue(HTTPS_DEFAULT_PORT);
                }
            }
            else if (getKey(P_WFE_SERVER_CONNECTOR_HOST_SUFFIX).equals(fieldEditor.getPreferenceName())) {
                String host = host(hostEditor.getStringValue());
                String protocol = protocolCombo.getItem(protocolCombo.getSelectionIndex());
                String oldPort = portEditor.getStringValue();
                if (LOCALHOST.equals(host.toLowerCase())) {
                    if (HTTPS.equals(protocol)) {
                        protocolCombo.select(0); // http
                        if (HTTPS_DEFAULT_PORT.equals(oldPort)) {
                            portEditor.setStringValue(HTTP_DEFAULT_PORT);
                        }
                    }
                } else {
                    if (HTTP.equals(protocol)) {
                        protocolCombo.select(1); // https
                        if (HTTP_DEFAULT_PORT.equals(oldPort)) {
                            portEditor.setStringValue(HTTPS_DEFAULT_PORT);
                        }
                    }
                }
            }
        }
    }

    private String host(String url) {
        int colonIndex = url.indexOf(':');
        if (colonIndex >= 0) {
            url = url.substring(colonIndex + 1);
            while (url.startsWith(SLASH)) {
                url = url.substring(1);
            }
        }
        int slashIndex = url.indexOf('/');
        if (slashIndex >= 0) {
            url = url.substring(0, slashIndex);
        }
        return url;
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
                    WfeServerConnectorsPreferenceNode wfeServerConnectorsNode = WfeServerConnectorsPreferenceNode.getInstance();
                    WfeServerConnectorPreferenceNode node = (WfeServerConnectorPreferenceNode) wfeServerConnectorsNode.findSubNode(id);
                    WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.load(node.getIndex()));
                    WfeServerConnector.getInstance().connect();
                    Dialogs.information(Localization.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.ConnectionFailed"), th);
                } finally {
                    WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.loadSelected());
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
                    if (!id.equals(WfeServerConnectorsPreferenceNode.getSelectedPrefix())) {
                        WfeServerConnectorsPreferenceNode wfeServerConnectorsNode = WfeServerConnectorsPreferenceNode.getInstance();
                        IPreferenceNode node = wfeServerConnectorsNode.findSubNode(id);
                        wfeServerConnectorsNode.remove(node);
                        wfeServerConnectorsNode.updateUi(getContainer(), WfeServerConnectorsPreferenceNode.getSelectedPrefix());
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
            String host = host(hostText.getText());
            hostText.setText(host);
        }
    };

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

    private String getKey(String property) {
        return id + '.' + property;
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        testButton.setEnabled(isValid());
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        WfeServerConnectorsPreferenceNode wfeServerConnectorsNode = WfeServerConnectorsPreferenceNode.getInstance();
        WfeServerConnectorPreferenceNode node = (WfeServerConnectorPreferenceNode) wfeServerConnectorsNode.findSubNode(id);
        node.updateName();
        wfeServerConnectorsNode.updateUi(getContainer(), node.getId());
        if (node.isSelected()) {
            WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.loadSelected());
        }
        return result;
    }

}

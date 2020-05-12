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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorSettings;
import ru.runa.gpd.ui.custom.Dialogs;

public class WfeServerConnectorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private final String id;
    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private StringFieldEditor hostEditor;
    private StringFieldEditor portEditor;
    private Button testButton;
    private Button deleteButton;

    public WfeServerConnectorPreferencePage(String id) {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
        this.id = id;
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    private static String[][] getProtocolEntriesArray() {
        String[][] strings = new String[2][2];
        strings[0][0] = "http";
        strings[0][1] = "http";
        strings[1][0] = "https";
        strings[1][1] = "https";
        return strings;
    }

    @Override
    public void createFieldEditors() {
        addField(new ComboFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX), Localization.getString("pref.connection.wfe.protocol"),
                getProtocolEntriesArray(), getFieldEditorParent()));
        hostEditor = new StringFieldEditor(getKey(P_WFE_SERVER_CONNECTOR_HOST_SUFFIX), Localization.getString("pref.connection.wfe.host"),
                getFieldEditorParent());
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
        }
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
                        wfeServerConnectorsNode.saveIndices();
                        wfeServerConnectorsNode.updateUi(WfeServerConnectorsPreferenceNode.getSelectedPrefix());
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

    @Override
    protected void initialize() {
        super.initialize();
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
        wfeServerConnectorsNode.updateUi(node.getId());
        if (node.isSelected()) {
            WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.loadSelected());
        }
        return result;
    }

}

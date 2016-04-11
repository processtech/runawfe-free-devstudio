package ru.runa.gpd.settings;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.wfe.WFEServerConnector;
import ru.runa.gpd.wfe.WFEServerConnectorRegistry;
import ru.runa.gpd.wfe.WFEServerConnectorRegistry.Entry;

public class WFEConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private StringFieldEditor portEditor;
    private Button testButton;

    public WFEConnectionPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new ComboFieldEditor(P_WFE_CONNECTION_TYPE, Localization.getString("pref.connection.wfe.type"),
                WFEServerConnectorRegistry.getEntriesArray(), getFieldEditorParent()));
        addField(new StringFieldEditor(P_WFE_CONNECTION_HOST, Localization.getString("pref.connection.wfe.host"), getFieldEditorParent()));
        portEditor = new StringFieldEditor(P_WFE_CONNECTION_PORT, Localization.getString("pref.connection.wfe.port"), getFieldEditorParent());
        addField(portEditor);
        addField(new StringFieldEditor(P_WFE_CONNECTION_VERSION, Localization.getString("pref.connection.wfe.version"), getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(P_WFE_CONNECTION_LOGIN_MODE, Localization.getString("pref.connection.loginMode"), 2, new String[][] {
                { Localization.getString("pref.connection.loginMode.byLogin"), LOGIN_MODE_LOGIN_PASSWORD },
                { Localization.getString("pref.connection.loginMode.byKerberos"), LOGIN_MODE_KERBEROS } }, getFieldEditorParent()));
        loginEditor = new StringFieldEditor(P_WFE_CONNECTION_LOGIN, Localization.getString("pref.connection.login"), getFieldEditorParent());
        passwordEditor = new StringFieldEditor(P_WFE_CONNECTION_PASSWORD, Localization.getString("pref.connection.password"), getFieldEditorParent());
        passwordEditor.setEmptyStringAllowed(true);
        boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_WFE_CONNECTION_LOGIN_MODE));
        loginEditor.setEnabled(enabled, getFieldEditorParent());
        passwordEditor.setEnabled(enabled, getFieldEditorParent());
        addField(loginEditor);
        addField(passwordEditor);
        addField(new BooleanFieldEditor(P_WFE_LOAD_PROCESS_DEFINITIONS_HISTORY,
                Localization.getString("pref.connection.wfe.load.process.definitions.history"), getFieldEditorParent()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_WFE_CONNECTION_LOGIN_MODE.equals(fieldEditor.getPreferenceName())) {
                boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(event.getNewValue());
                loginEditor.setEnabled(enabled, getFieldEditorParent());
                passwordEditor.setEnabled(enabled, getFieldEditorParent());
            }
            if (P_WFE_CONNECTION_TYPE.equals(fieldEditor.getPreferenceName())) {
                WFEServerConnector.destroy();
                Entry entry = WFEServerConnectorRegistry.getEntryNotNull((String) event.getNewValue());
                setMessage(entry.description);
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
                    WFEServerConnector.getInstance().connect();
                    Dialogs.information(Localization.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.ConnectionFailed"), th);
                }
            }
        });
        testButton.setEnabled(isValid());
        applyDialogFont(buttonBar);
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        testButton.setEnabled(isValid());
    }
}

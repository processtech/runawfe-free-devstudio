package ru.runa.gpd.ldap;

import org.eclipse.jface.dialogs.Dialog;
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
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;

public class LDAPConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {

    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private Button testButton;

    public LDAPConnectionPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.ldap.title"));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new StringFieldEditor(P_LDAP_CONNECTION_PROVIDER_URL, Localization.getString("pref.connection.ldap.server"), getFieldEditorParent()));
        addField(new StringFieldEditor(P_LDAP_CONNECTION_OU, Localization.getString("pref.connection.ldap.ou"), getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(P_LDAP_CONNECTION_LOGIN_MODE, Localization.getString("pref.connection.loginMode"), 2, new String[][] {
                { Localization.getString("pref.connection.loginMode.byLogin"), LOGIN_MODE_LOGIN_PASSWORD },
                { Localization.getString("pref.connection.loginMode.byKerberos"), LOGIN_MODE_KERBEROS } }, getFieldEditorParent()));
        loginEditor = new StringFieldEditor(P_LDAP_CONNECTION_LOGIN, Localization.getString("pref.connection.login"), getFieldEditorParent());
        passwordEditor = new StringFieldEditor(P_LDAP_CONNECTION_PASSWORD, Localization.getString("pref.connection.password"), getFieldEditorParent());
        passwordEditor.setEmptyStringAllowed(true);
        passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
        boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_LDAP_CONNECTION_LOGIN_MODE));
        loginEditor.setEnabled(enabled, getFieldEditorParent());
        passwordEditor.setEnabled(enabled, getFieldEditorParent());
        addField(loginEditor);
        addField(passwordEditor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_LDAP_CONNECTION_LOGIN_MODE.equals(fieldEditor.getPreferenceName())) {
                boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(event.getNewValue());
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
                    LDAPConnector.getInstance().connect();
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

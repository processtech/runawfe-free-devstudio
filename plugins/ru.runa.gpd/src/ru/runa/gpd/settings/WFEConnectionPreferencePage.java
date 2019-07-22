package ru.runa.gpd.settings;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.WFEListConnectionsModel.ConItem;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerConnector;

public class WFEConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private final String id;
    private final boolean isWorkbenchPreference;
    private StringFieldEditor loginEditor;
    private StringFieldEditor passwordEditor;
    private StringFieldEditor hostEditor;
    private StringFieldEditor portEditor;
    private Button testButton;
    private Button deleteButton;

    public WFEConnectionPreferencePage(String id) {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
        this.id = id;
        this.isWorkbenchPreference = false;
    }

    public WFEConnectionPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
        this.id = Activator.getPrefString(P_WFE_LIST_CONNECTIONS);
        this.isWorkbenchPreference = true;
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
        addField(new ComboFieldEditor(getKey(P_WFE_CONNECTION_PROTOCOL), Localization.getString("pref.connection.wfe.protocol"),
                getProtocolEntriesArray(), getFieldEditorParent()));
        hostEditor = new StringFieldEditor(getKey(P_WFE_CONNECTION_HOST), Localization.getString("pref.connection.wfe.host"), getFieldEditorParent());
        addField(hostEditor);
        portEditor = new StringFieldEditor(getKey(P_WFE_CONNECTION_PORT), Localization.getString("pref.connection.wfe.port"), getFieldEditorParent());
        addField(portEditor);
        addField(new StringFieldEditor(getKey(P_WFE_CONNECTION_VERSION), Localization.getString("pref.connection.wfe.version"),
                getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(getKey(P_WFE_CONNECTION_LOGIN_MODE), Localization.getString("pref.connection.loginMode"), 2,
                new String[][] { { Localization.getString("pref.connection.loginMode.byLogin"), LOGIN_MODE_LOGIN_PASSWORD },
                        { Localization.getString("pref.connection.loginMode.byKerberos"), LOGIN_MODE_KERBEROS } },
                getFieldEditorParent()));
        loginEditor = new StringFieldEditor(getKey(P_WFE_CONNECTION_LOGIN), Localization.getString("pref.connection.login"), getFieldEditorParent());
        passwordEditor = new StringFieldEditor(getKey(P_WFE_CONNECTION_PASSWORD), Localization.getString("pref.connection.password"),
                getFieldEditorParent());
        passwordEditor.setEmptyStringAllowed(true);
        passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');
        boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(getKey(P_WFE_CONNECTION_LOGIN_MODE)));
        loginEditor.setEnabled(enabled, getFieldEditorParent());
        passwordEditor.setEnabled(enabled, getFieldEditorParent());
        addField(loginEditor);
        addField(passwordEditor);
        addField(new BooleanFieldEditor(getKey(P_WFE_LOAD_PROCESS_DEFINITIONS_HISTORY),
                Localization.getString("pref.connection.wfe.load.process.definitions.history"), getFieldEditorParent()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (getKey(P_WFE_CONNECTION_LOGIN_MODE).equals(fieldEditor.getPreferenceName())) {
                boolean enabled = LOGIN_MODE_LOGIN_PASSWORD.equals(event.getNewValue());
                loginEditor.setEnabled(enabled, getFieldEditorParent());
                passwordEditor.setEnabled(enabled, getFieldEditorParent());
            }
        }
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        String id = this.id;
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        testButton = new Button(buttonBar, SWT.PUSH);
        testButton.setText(Localization.getString("button.test.connection"));
        Dialog.applyDialogFont(testButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        testButton.setLayoutData(data);
        testButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String actCon = Activator.getPrefString(P_WFE_LIST_CONNECTIONS);
                boolean eq = actCon.equals(id);
                try {
                    performApply();
                    if (!eq)
                        Activator.getDefault().getPreferenceStore().setValue(P_WFE_LIST_CONNECTIONS, id);
                    WFEServerConnector.getInstance().connect();
                    Dialogs.information(Localization.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.ConnectionFailed"), th);
                } finally {
                    if (!eq)
                        Activator.getDefault().getPreferenceStore().setValue(P_WFE_LIST_CONNECTIONS, actCon);
                    DataImporter.clearCache();
                }
            }
        });
        testButton.setEnabled(isValid());
        applyDialogFont(buttonBar);

        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode integrationNode = preferenceManager.find("gpd.pref.connection");
        IPreferenceNode connectionsPreferenceNode = integrationNode.findSubNode(WFEListConnectionsPreferenceNode.ID);
        IPreferenceNode node = connectionsPreferenceNode.findSubNode(this.id);

        if (!isWorkbenchPreference) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            PreferenceDialog pd = PreferencesUtil.createPreferenceDialogOn(shell, node.getId(), null, null);
            ((GridLayout) buttonBar.getLayout()).numColumns++;
            deleteButton = new Button(buttonBar, SWT.PUSH);
            deleteButton.setText(Localization.getString("button.delete.connection"));
            Dialog.applyDialogFont(deleteButton);
            deleteButton.setLayoutData(data);
            deleteButton.addSelectionListener(new SelectionAdapter() {
                private TreeItem getItemByID(TreeItem[] items, String id) {
                    TreeItem ti_g = null;
                    for (TreeItem ti : items) {
                        if (((IPreferenceNode) ti.getData()).getId().equals(id)) {
                            ti_g = ti;
                            break;
                        }
                    }
                    return ti_g;
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    String actCon = Activator.getPrefString(P_WFE_LIST_CONNECTIONS);
                    try {
                        if (!(actCon).equals(id)) {
                            int prev = ((WFEConnectionPreferenceNode) node).getPrevious();
                            int next = ((WFEConnectionPreferenceNode) node).getNext();

                            if (prev != 0) {
                                ((WFEConnectionPreferenceNode) connectionsPreferenceNode.findSubNode(WFEConnectionPreferenceNode.genId(prev)))
                                        .setNext(next);
                            } else {
                                ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).setHead(next);
                            }

                            if (next != 0) {
                                ((WFEConnectionPreferenceNode) connectionsPreferenceNode.findSubNode(WFEConnectionPreferenceNode.genId(next)))
                                        .setPrevious(prev);
                            } else {
                                ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).setTale(prev);
                            }

                            connectionsPreferenceNode.remove(node);
                            WFEListConnectionsModel.getInstance().removeWFEConnection(new ConItem(node.getLabelText(), node.getId()));

                            pd.getTreeViewer().refresh();

                            TreeItem ti_integ = getItemByID(pd.getTreeViewer().getTree().getItems(), "gpd.pref.connection");
                            TreeItem ti_wfe = getItemByID(ti_integ.getItems(), WFEListConnectionsPreferenceNode.ID);
                            pd.getTreeViewer().getTree().select(ti_wfe);
                        } else {
                            Dialogs.warning(Localization.getString("warn.DeleteConnection"));
                        }

                    } catch (Throwable th) {
                        Dialogs.error(Localization.getString("error.DeleteConnection.Failed"), th);
                    }
                }
            });
            deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            deleteButton.setEnabled(isValid());
            applyDialogFont(buttonBar);
        }
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
        DataImporter.clearCache();

        StringBuilder newName = new StringBuilder();
        String host = hostEditor.getStringValue();
        String port = portEditor.getStringValue();

        if (!host.isEmpty())
            newName.append(host);
        else
            newName.append("[host]");

        newName.append(" : ");

        if (!port.isEmpty())
            newName.append(port);
        else
            newName.append("[port]");

        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode integrationNode = preferenceManager.find("gpd.pref.connection");
        IPreferenceNode connectionsPreferenceNode = integrationNode.findSubNode(WFEListConnectionsPreferenceNode.ID);
        IPreferenceNode node = connectionsPreferenceNode.findSubNode(this.id);
        ((WFEConnectionPreferenceNode) node).setName(newName.toString());

        WFEListConnectionsModel.getInstance().updateWFEConnection(new ConItem(node.getLabelText(), node.getId()));

        if (!isWorkbenchPreference) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            PreferenceDialog pd = PreferencesUtil.createPreferenceDialogOn(shell, node.getId(), null, null);
            pd.getTreeViewer().refresh();
        }

        return super.performOk();
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }
}

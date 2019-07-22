package ru.runa.gpd.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
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

public class WFEListConnectionsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private Button addButton;
    public WFEListConnectionsFieldEditor comboConnections;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private boolean bSendEvent = false;
    private Object oldVal;
    private Object newVal;
    private String prop;

    public WFEListConnectionsPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
    }

    @Override
    public void init(IWorkbench workbench) {
        Dialogs.information("init");
    }

    @Override
    public void createFieldEditors() {
        comboConnections = new WFEListConnectionsFieldEditor(P_WFE_LIST_CONNECTIONS, Localization.getString("pref.defaultconnection.wfe.label"),
                getFieldEditorParent());
        addField(comboConnections);
    }

    private void initNodePreferences(String id) {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(id + "." + P_WFE_CONNECTION_PROTOCOL, "http");
        store.setDefault(id + "." + P_WFE_CONNECTION_HOST, "localhost");
        store.setDefault(id + "." + P_WFE_CONNECTION_PORT, "8080");
        store.setDefault(id + "." + P_WFE_CONNECTION_VERSION, "auto");
        store.setDefault(id + "." + P_WFE_CONNECTION_LOGIN_MODE, LOGIN_MODE_LOGIN_PASSWORD);
        store.setDefault(id + "." + P_WFE_CONNECTION_LOGIN, "Administrator");
        store.setDefault(id + "." + P_WFE_CONNECTION_PASSWORD, "wf");
        store.setDefault(id + "." + P_WFE_LOAD_PROCESS_DEFINITIONS_HISTORY, false);
        store.setValue(id + "." + P_WFE_CONNECTION_HOST, "");
        store.setValue(id + "." + P_WFE_CONNECTION_PORT, "");
        store.setValue(id + "." + P_WFE_CONNECTION_LOGIN, "");
        store.setValue(id + "." + P_WFE_CONNECTION_PASSWORD, "");
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_WFE_LIST_CONNECTIONS.equals(fieldEditor.getPreferenceName())) {
                bSendEvent = true;
                oldVal = event.getOldValue();
                newVal = event.getNewValue();
                prop = event.getProperty();
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

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

    private void selectNode(PreferenceNode node) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        PreferenceDialog pd = PreferencesUtil.createPreferenceDialogOn(shell, node.getId(), null, null);
        pd.getTreeViewer().refresh();

        TreeItem ti_integ = getItemByID(pd.getTreeViewer().getTree().getItems(), "gpd.pref.connection");
        TreeItem ti_wfe = getItemByID(ti_integ.getItems(), WFEListConnectionsPreferenceNode.ID);
        ti_wfe.setExpanded(true);
        pd.getTreeViewer().refresh();
        TreeItem ti_con = getItemByID(ti_wfe.getItems(), node.getId());
        pd.getTreeViewer().getTree().select(ti_con);
    }

    private PreferenceNode addConnection() {
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode integrationNode = preferenceManager.find("gpd.pref.connection");
        IPreferenceNode connectionsPreferenceNode = integrationNode.findSubNode(WFEListConnectionsPreferenceNode.ID);

        int i;
        for (i = 1; i < connectionsPreferenceNode.getSubNodes().length + 1; i++) {
            if (connectionsPreferenceNode.findSubNode(WFEConnectionPreferenceNode.genId(i)) == null) {
                break;
            }
        }
        initNodePreferences(WFEConnectionPreferenceNode.genId(i));

        int tail = ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).getTale();
        PreferenceNode node = new WFEConnectionPreferenceNode(i);
        ((WFEConnectionPreferenceNode) node).setPrevious(tail);
        ((WFEConnectionPreferenceNode) connectionsPreferenceNode.findSubNode(WFEConnectionPreferenceNode.genId(tail))).setNext(i);
        ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).setTale(i);
        ((WFEConnectionPreferenceNode) node).setNext(0);
        connectionsPreferenceNode.add(node);

        WFEListConnectionsModel.getInstance().addWFEConnection(new ConItem(node.getLabelText(), node.getId()));

        return node;
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        addButton = new Button(buttonBar, SWT.PUSH);
        addButton.setText(Localization.getString("button.add.connection"));
        Dialog.applyDialogFont(addButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        addButton.setLayoutData(data);
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    PreferenceNode node = addConnection();
                    selectNode(node);
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.AddConnection.Failed"), th);
                }
            }
        });
        addButton.setEnabled(isValid());
        applyDialogFont(buttonBar);
    }

    @Override
    protected void performDefaults() {
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        addButton.setEnabled(isValid());
    }

    @Override
    public boolean performOk() {
        if (bSendEvent) {
            changes.firePropertyChange(prop, oldVal, newVal);
            bSendEvent = false;
        }
        DataImporter.clearCache();
        return super.performOk();
    }
}

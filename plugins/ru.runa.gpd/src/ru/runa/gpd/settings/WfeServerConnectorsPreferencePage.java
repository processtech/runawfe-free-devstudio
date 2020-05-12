package ru.runa.gpd.settings;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorSettings;
import ru.runa.gpd.ui.custom.Dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

public class WfeServerConnectorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private ComboFieldEditor2 fieldEditor;
    private Button addButton;

    public WfeServerConnectorsPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Localization.getString("pref.connection.wfe.title"));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            fieldEditor.updateData(WfeServerConnectorsPreferenceNode.getComboItems());
            fieldEditor.doLoad();
        }
    }

    @Override
    public void createFieldEditors() {
        fieldEditor = new ComboFieldEditor2(P_WFE_SERVER_CONNECTOR_SELECTED_INDEX, Localization.getString("pref.connection.wfe.selected.label"),
                WfeServerConnectorsPreferenceNode.getComboItems(), getFieldEditorParent());
        addField(fieldEditor);
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
                    WfeServerConnectorsPreferenceNode wfeServerConnectorsNode = WfeServerConnectorsPreferenceNode.getInstance();
                    IPreferenceNode[] children = wfeServerConnectorsNode.getSubNodes();
                    WfeServerConnectorPreferenceNode lastNode = (WfeServerConnectorPreferenceNode) children[children.length - 1];
                    PreferenceNode node = new WfeServerConnectorPreferenceNode(lastNode.getIndex() + 1);
                    wfeServerConnectorsNode.add(node);
                    wfeServerConnectorsNode.saveIndices();
                    initNodePreferences(node.getId());
                    wfeServerConnectorsNode.updateUi(node.getId());
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error"), th);
                }
            }
        });
        applyDialogFont(buttonBar);
    }

    private void initNodePreferences(String prefix) {
        IPreferenceStore store = getPreferenceStore();
        WfeServerConnectorSettings connectorSettings = WfeServerConnectorSettings.createDefault();
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX, connectorSettings.getProtocol());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX, connectorSettings.getHost());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX, connectorSettings.getPort());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX, connectorSettings.getAuthenticationType());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX, connectorSettings.getLogin());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX, connectorSettings.getPassword());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX,
                connectorSettings.isLoadProcessDefinitionsHistory());
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX,
                connectorSettings.isAllowUpdateLastVersionByKeyBinding());
    }

    @Override
    protected void performDefaults() {
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.loadSelected());
        return result;
    }

    /**
     * Copied from org.eclipse.jface.preference.ComboFieldEditor to reflect changed in source data.
     */
    private class ComboFieldEditor2 extends FieldEditor {

        /**
         * The <code>Combo</code> widget.
         */
        private Combo fCombo;

        /**
         * The value (not the name) of the currently selected item in the Combo widget.
         */
        private String fValue;

        /**
         * The names (labels) and underlying values to populate the combo widget. These should be arranged as: { {name1, value1}, {name2, value2},
         * ...}
         */
        private String[][] fEntryNamesAndValues;

        /**
         * Create the combo box field editor.
         *
         * @param name
         *            the name of the preference this field editor works on
         * @param labelText
         *            the label text of the field editor
         * @param entryNamesAndValues
         *            the names (labels) and underlying values to populate the combo widget. These should be arranged as: { {name1, value1}, {name2,
         *            value2}, ...}
         * @param parent
         *            the parent composite
         */
        public ComboFieldEditor2(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
            init(name, labelText);
            Assert.isTrue(checkArray(entryNamesAndValues));
            fEntryNamesAndValues = entryNamesAndValues;
            createControl(parent);
        }

        /**
         * Checks whether given <code>String[][]</code> is of "type" <code>String[][2]</code>.
         *
         * @return <code>true</code> if it is ok, and <code>false</code> otherwise
         */
        private boolean checkArray(String[][] table) {
            if (table == null) {
                return false;
            }
            for (String[] array : table) {
                if (array == null || array.length != 2) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            if (numColumns > 1) {
                Control control = getLabelControl();
                int left = numColumns;
                if (control != null) {
                    ((GridData) control.getLayoutData()).horizontalSpan = 1;
                    left = left - 1;
                }
                ((GridData) fCombo.getLayoutData()).horizontalSpan = left;
            } else {
                Control control = getLabelControl();
                if (control != null) {
                    ((GridData) control.getLayoutData()).horizontalSpan = 1;
                }
                ((GridData) fCombo.getLayoutData()).horizontalSpan = 1;
            }
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            int comboC = 1;
            if (numColumns > 1) {
                comboC = numColumns - 1;
            }
            Control control = getLabelControl(parent);
            GridData gd = new GridData();
            gd.horizontalSpan = 1;
            control.setLayoutData(gd);
            control = getComboBoxControl(parent);
            gd = new GridData();
            gd.horizontalSpan = comboC;
            gd.horizontalAlignment = GridData.FILL;
            control.setLayoutData(gd);
            control.setFont(parent.getFont());
        }

        @Override
        protected void doLoad() {
            updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
        }

        @Override
        protected void doLoadDefault() {
            updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
        }

        @Override
        protected void doStore() {
            if (fValue == null) {
                getPreferenceStore().setToDefault(getPreferenceName());
                return;
            }
            getPreferenceStore().setValue(getPreferenceName(), fValue);
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }

        /*
         * Lazily create and return the Combo control.
         */
        private Combo getComboBoxControl(Composite parent) {
            if (fCombo == null) {
                fCombo = new Combo(parent, SWT.READ_ONLY);
                fCombo.setFont(parent.getFont());
                for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                    fCombo.add(fEntryNamesAndValues[i][0], i);
                }

                fCombo.addSelectionListener(widgetSelectedAdapter(evt -> {
                    String oldValue = fValue;
                    String name = fCombo.getText();
                    fValue = getValueForName(name);
                    setPresentsDefaultValue(false);
                    fireValueChanged(VALUE, oldValue, fValue);
                }));
            }
            return fCombo;
        }

        private void updateData(String[][] entryNamesAndValues) {
            this.fEntryNamesAndValues = entryNamesAndValues;
            fCombo.removeAll();
            for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                fCombo.add(fEntryNamesAndValues[i][0], i);
            }
        }

        /*
         * Given the name (label) of an entry, return the corresponding value.
         */
        private String getValueForName(String name) {
            for (String[] entry : fEntryNamesAndValues) {
                if (name.equals(entry[0])) {
                    return entry[1];
                }
            }
            return fEntryNamesAndValues[0][0];
        }

        /*
         * Set the name in the combo widget to match the specified value.
         */
        private void updateComboForValue(String value) {
            fValue = value;
            for (String[] fEntryNamesAndValue : fEntryNamesAndValues) {
                if (value.equals(fEntryNamesAndValue[1])) {
                    fCombo.setText(fEntryNamesAndValue[0]);
                    return;
                }
            }
            if (fEntryNamesAndValues.length > 0) {
                fValue = fEntryNamesAndValues[0][1];
                fCombo.setText(fEntryNamesAndValues[0][0]);
            }
        }

        @Override
        public void setEnabled(boolean enabled, Composite parent) {
            super.setEnabled(enabled, parent);
            getComboBoxControl(parent).setEnabled(enabled);
        }
    }

}

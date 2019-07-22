package ru.runa.gpd.settings;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import ru.runa.gpd.settings.WFEListConnectionsModel.ConItem;
import ru.runa.gpd.settings.WFEListConnectionsModel;

public class WFEListConnectionsFieldEditor extends FieldEditor {

    private ComboViewer combo;
    private String fValue;

    public WFEListConnectionsFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        createControl(parent);
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
            ((GridData) combo.getCombo().getLayoutData()).horizontalSpan = left;
        } else {
            Control control = getLabelControl();
            if (control != null) {
                ((GridData) control.getLayoutData()).horizontalSpan = 1;
            }
            ((GridData) combo.getCombo().getLayoutData()).horizontalSpan = 1;
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

    private Combo getComboBoxControl(Composite parent) {
        if (combo == null) {
            combo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
            combo.getCombo().setFont(parent.getFont());
            combo.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element instanceof ConItem) {
                        ConItem current = (ConItem) element;
                        return current.getLabel();
                    }
                    return "";
                }
            });
            combo.setContentProvider(new ObservableListContentProvider());
            combo.setInput(WFEListConnectionsModel.getInstance().getWFEConnections());
            combo.getCombo().addSelectionListener(widgetSelectedAdapter(evt -> {
                String oldValue = fValue;
                IStructuredSelection selection = combo.getStructuredSelection();
                ConItem item = (ConItem) selection.getFirstElement();
                fValue = item.getValue();
                setPresentsDefaultValue(false);
                fireValueChanged(VALUE, oldValue, fValue);
            }));
        }
        return combo.getCombo();
    }

    private void updateComboForValue(String value) {
        fValue = value;
        for (ConItem item : WFEListConnectionsModel.getInstance().getWFEConnections()) {
            if (value.equals(item.getValue())) {
                ISelection selection = new StructuredSelection(item);
                combo.setSelection(selection);
                return;
            }
        }

        if (WFEListConnectionsModel.getInstance().getWFEConnections().size() > 0) {
            ConItem item = WFEListConnectionsModel.getInstance().getWFEConnections().get(0);
            fValue = item.getValue();
            ISelection selection = new StructuredSelection(item);
            combo.setSelection(selection);
        }
    }

    @Override
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        getComboBoxControl(parent).setEnabled(enabled);
    }
}

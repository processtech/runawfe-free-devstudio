package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ui.RichComboDialog;

public class RichComboPropertyDescriptor extends PropertyDescriptor {
    private final ComponentParameter parameter;
    private final List<String> variableNames;

    public RichComboPropertyDescriptor(Object id, ComponentParameter parameter, List<String> variableNames) {
        super(id, parameter.getLabel());
        this.parameter = parameter;
        this.variableNames = variableNames;
        setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                String value = (String) element;
                if (value == null) {
                    value = "";
                }
                if (value.startsWith(RichComboParameter.VALUE_PREFIX)) {
                    value = value.substring(RichComboParameter.VALUE_PREFIX.length());
                }
                return " " + value;
            }

        });
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new RichComboCellEditor(parent);
    }

    private class RichComboCellEditor extends DialogCellEditor {

        public RichComboCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            RichComboDialog dialog = new RichComboDialog(parameter.getVariableTypeFilter(), variableNames, (String) doGetValue());
            return dialog.openDialog();
        }

        @Override
        protected void updateContents(Object value) {
            if (getDefaultLabel() != null) {
                getDefaultLabel().setText(getLabelProvider().getText(value));
            }
        }
    }

}

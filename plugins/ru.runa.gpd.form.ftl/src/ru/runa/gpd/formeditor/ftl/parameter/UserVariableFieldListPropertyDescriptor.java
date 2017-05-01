package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.ui.UserVariableFieldListDialog;

public class UserVariableFieldListPropertyDescriptor extends PropertyDescriptor {
    private final List<String> attributeNames;

    public UserVariableFieldListPropertyDescriptor(Object id, String displayName, List<String> atributeNames) {
        super(id, displayName);
        this.attributeNames = atributeNames;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new VariableListCellEditor(parent);
    }

    private class VariableListCellEditor extends DialogCellEditor {

        public VariableListCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            List<String> value = (List<String>) doGetValue();
            UserVariableFieldListDialog dialog = new UserVariableFieldListDialog(attributeNames, value);
            return dialog.openDialog();
        }

    }

}

package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ui.UserTypeAttributeListDialog;

public class UserTypeAttributeListPropertyDescriptor extends PropertyDescriptor {
    private final Component component;
    private final UserTypeAttributeListParameterType parameter;

    public UserTypeAttributeListPropertyDescriptor(Object id, String displayName, Component component, UserTypeAttributeListParameterType parameter) {
        super(id, displayName);
        this.component = component;
        this.parameter = parameter;
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
            List<String> value = parameter.convertValueToList(doGetValue());
            UserTypeAttributeListDialog dialog = new UserTypeAttributeListDialog(parameter.getAttributes(component), value);
            return dialog.openDialog();
        }

    }

}

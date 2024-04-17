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
import ru.runa.gpd.lang.model.ProcessDefinition;

public class UserTypeAttributeListPropertyDescriptor extends PropertyDescriptor {
    private final Component component;
    private final UserTypeAttributeListParameterType parameter;
    private ProcessDefinition processDefinition;

    public UserTypeAttributeListPropertyDescriptor(Object id, String displayName, Component component, UserTypeAttributeListParameterType parameter,
            ProcessDefinition processDefinition) {
        super(id, displayName);
        this.component = component;
        this.parameter = parameter;
        this.processDefinition = processDefinition;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new VariableListCellEditor(parent, this.processDefinition);
    }

    private class VariableListCellEditor extends DialogCellEditor {
        private ProcessDefinition processDefinition;

        public VariableListCellEditor(Composite parent, ProcessDefinition processDefinition) {
            super(parent, SWT.NONE);
            this.processDefinition = processDefinition;
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            List<String> value = parameter.convertValueToList(doGetValue());
            UserTypeAttributeListDialog dialog = new UserTypeAttributeListDialog(parameter.getAttributes(component, this.processDefinition), value);
            return dialog.openDialog();
        }

    }

}

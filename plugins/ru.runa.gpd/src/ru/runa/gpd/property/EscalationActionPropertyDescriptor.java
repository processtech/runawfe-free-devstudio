package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.EscalationActionEditDialog;

public class EscalationActionPropertyDescriptor extends PropertyDescriptor {
    private final TaskState element;

    public EscalationActionPropertyDescriptor(Object id, String label, TaskState element) {
        super(id, label);
        this.element = element;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new EscalationActionDialogCellEditor(parent);
    }

    private class EscalationActionDialogCellEditor extends DialogCellEditor {
        public EscalationActionDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            EscalationActionEditDialog dialog = new EscalationActionEditDialog(element);
            return dialog.openDialog();
        }
    }
}

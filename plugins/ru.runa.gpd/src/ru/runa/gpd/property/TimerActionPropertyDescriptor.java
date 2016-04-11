package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class TimerActionPropertyDescriptor extends PropertyDescriptor {
    private final Timer timer;

    public TimerActionPropertyDescriptor(Object id, String label, Timer timer) {
        super(id, label);
        this.timer = timer;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new TimerActionDialogCellEditor(parent);
    }

    private class TimerActionDialogCellEditor extends DialogCellEditor {
        public TimerActionDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            TimerActionEditDialog dialog = new TimerActionEditDialog(timer.getProcessDefinition(), timer.getAction());
            return dialog.openDialog();
        }
    }
}

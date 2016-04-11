package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class DurationPropertyDescriptor extends PropertyDescriptor {
    private final ProcessDefinition definition;
    private final Duration duration;

    public DurationPropertyDescriptor(Object id, ProcessDefinition definition, Duration duration, String label) {
        super(id, label);
        this.definition = definition;
        this.duration = duration;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new DurationDialogCellEditor(parent);
    }

    private class DurationDialogCellEditor extends DialogCellEditor {
        public DurationDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            DurationEditDialog dialog = new DurationEditDialog(definition, duration);
            return dialog.openDialog();
        }
    }
}

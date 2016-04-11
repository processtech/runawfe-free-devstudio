package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.ui.TextListDialog;

public class TextListPropertyDescriptor extends PropertyDescriptor {

    public TextListPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new TextListCellEditor(parent);
    }

    private class TextListCellEditor extends DialogCellEditor {

        public TextListCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            List<String> value = (List<String>) doGetValue();
            TextListDialog dialog = new TextListDialog(value);
            return dialog.openDialog();
        }

    }

}

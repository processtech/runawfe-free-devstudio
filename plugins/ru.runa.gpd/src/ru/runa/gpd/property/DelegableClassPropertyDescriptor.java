package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.extension.DelegableClassLabelProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;

public class DelegableClassPropertyDescriptor extends PropertyDescriptor {
    private final Delegable delegable;

    public DelegableClassPropertyDescriptor(Object id, String label, Delegable delegable) {
        super(id, label);
        this.delegable = delegable;
        setLabelProvider(new DelegableClassLabelProvider(true));
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ChooseClassDialogCellEditor(parent);
    }

    private class ChooseClassDialogCellEditor extends DialogCellEditor {
        public ChooseClassDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(delegable.getDelegationType(), delegable.getDelegationClassName());
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

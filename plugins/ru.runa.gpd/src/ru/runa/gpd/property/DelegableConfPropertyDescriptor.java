package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;

public class DelegableConfPropertyDescriptor extends PropertyDescriptor {
    private final Delegable delegable;

    public DelegableConfPropertyDescriptor(Object id, Delegable delegable, String label) {
        super(id, label);
        this.delegable = delegable;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new DelegableCellEditor(parent);
    }

    private class DelegableCellEditor extends DialogCellEditor {
        public DelegableCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            try {
                DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
                return provider.showConfigurationDialog(delegable, null);
            } catch (Exception e) {
                PluginLogger.logError("Unable to open configuration dialog for " + delegable.getDelegationClassName(), e);
                return null;
            }
        }

        @Override
        protected void updateContents(Object value) {
            if (getDefaultLabel() != null) {
                String text = value != null ? (String) value : "";
                String firstLine;
                if (text.indexOf("\n") > 0) {
                    firstLine = text.substring(0, text.indexOf("\n"));
                } else {
                    firstLine = text;
                }
                getDefaultLabel().setText(firstLine);
                getDefaultLabel().setToolTipText(text);
            }
        }
    }
}

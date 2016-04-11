package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.ProcessEditorBase;

public class EnableAntialiasingAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        Activator.getDefault().getDialogSettings().put(PluginConstants.DISABLE_ANTIALIASING, !action.isChecked());
        for (ProcessEditorBase editor : getOpenedDesignerEditors()) {
            editor.refresh();
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setChecked(!Activator.getDefault().getDialogSettings().getBoolean(PluginConstants.DISABLE_ANTIALIASING));
    }
}

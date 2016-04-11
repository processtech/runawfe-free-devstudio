package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public class Save extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        getActiveEditor().doSave(null);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(getActiveEditor() != null && getActiveEditor().isDirty());
    }
}

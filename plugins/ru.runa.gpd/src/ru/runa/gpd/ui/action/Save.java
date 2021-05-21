package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.editor.DirtyDependentActions;

public class Save extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        getActiveEditor().doSave(null);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        DirtyDependentActions.add(this, action);
        action.setEnabled(getActiveEditor() != null && getActiveEditor().isDirty());
    }

    @Override
    public void dispose() {
        DirtyDependentActions.remove(this);
        super.dispose();
    }
}

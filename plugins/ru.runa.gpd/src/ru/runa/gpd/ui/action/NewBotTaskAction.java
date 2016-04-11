package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.util.WorkspaceOperations;

public class NewBotTaskAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        WorkspaceOperations.createNewBotTask(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isBotStructuredSelection());
    }
}

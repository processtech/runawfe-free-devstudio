package ru.runa.gpd.ui.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import ru.runa.gpd.util.WorkspaceOperations;

public class DeleteAction extends BaseActionDelegate {
    @Override
    @SuppressWarnings("unchecked")
    public void run(IAction action) {
        IStructuredSelection selection = getStructuredSelection();
        if (isBotStructuredSelection()) {
            WorkspaceOperations.deleteBotResources(selection.toList());
        } else {
            WorkspaceOperations.deleteResources(selection.toList());
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        boolean actionEnabled = false;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (!structuredSelection.isEmpty() && structuredSelection.getFirstElement() instanceof IResource) {
                actionEnabled = true;
            }
        }
        action.setEnabled(actionEnabled);
    }
}

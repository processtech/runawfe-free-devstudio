package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class NewProcessAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        WorkspaceOperations.createNewProcessDefinition(getStructuredSelection(), ProcessDefinitionAccessType.Process);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(IOUtils.getAllProcessDefinitionProjects().length > 0 && !isBotStructuredSelection());
    }
}

package ru.runa.gpd.ui.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import ru.runa.gpd.ui.wizard.ExportBotStationWizardPage;
import ru.runa.gpd.ui.wizard.ExportBotTaskWizardPage;
import ru.runa.gpd.ui.wizard.ExportBotWizardPage;
import ru.runa.gpd.util.WorkspaceOperations;

public class ExportBotElementAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        IStructuredSelection selection = getStructuredSelection();
        final Object selectedObject = selection.getFirstElement();
        boolean menuOnBotStation = selectedObject instanceof IProject;
        boolean menuOnBot = selectedObject instanceof IFolder;
        boolean menuOnBotTask = selectedObject instanceof IFile;
        if (menuOnBotStation) {
            WorkspaceOperations.exportBotElement(selection, new ExportBotStationWizardPage(selection));
        }
        if (menuOnBot) {
            WorkspaceOperations.exportBotElement(selection, new ExportBotWizardPage(selection));
        }
        if (menuOnBotTask) {
            WorkspaceOperations.exportBotElement(selection, new ExportBotTaskWizardPage(selection));
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isBotStructuredSelection());
    }
}

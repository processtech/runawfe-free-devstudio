package ru.runa.gpd.ui.action;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.ui.wizard.ImportBotStationWizardPage;
import ru.runa.gpd.ui.wizard.ImportBotTaskWizardPage;
import ru.runa.gpd.ui.wizard.ImportBotWizardPage;
import ru.runa.gpd.util.WorkspaceOperations;

public class ImportBotElementAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        final IStructuredSelection selection = getStructuredSelection();
        final Object selectedObject = selection.getFirstElement();
        boolean menuOnBotStation;
        try {
            menuOnBotStation = selectedObject instanceof IProject && ((IProject) selectedObject).getNature(BotStationNature.NATURE_ID) != null;
        } catch (CoreException e) {
            menuOnBotStation = false;
        }
        boolean menuOnBot = selectedObject instanceof IFolder;
        if (menuOnBotStation) {
            WorkspaceOperations.importBotElement(selection, new ImportBotWizardPage(selection));
        } else if (menuOnBot) {
            WorkspaceOperations.importBotElement(selection, new ImportBotTaskWizardPage(selection));
        } else {
            WorkspaceOperations.importBotElement(selection, new ImportBotStationWizardPage(selection));
        }
    }
}

package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.ui.view.ProcessExplorerTreeView;
import ru.runa.gpd.util.WorkspaceOperations;

public class ExportProcessAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        WorkspaceOperations.exportProcessDefinition(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(ProcessCache.getAllProcessDefinitions().size() > 0 && !isBotStructuredSelection());
    }

    @Override
    protected IStructuredSelection getStructuredSelection() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart viewPart = page.findView(ProcessExplorerTreeView.ID);
        return viewPart != null ? (IStructuredSelection) viewPart.getSite().getSelectionProvider().getSelection() : StructuredSelection.EMPTY;
    }
}

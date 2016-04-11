package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.PluginLogger;

public abstract class OpenViewBaseAction extends BaseActionDelegate {

    protected abstract String getViewId();

    @Override
    public void run(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(getViewId(), null, IWorkbenchPage.VIEW_VISIBLE);
        } catch (PartInitException e) {
            PluginLogger.logError(e);
        }
    }

}

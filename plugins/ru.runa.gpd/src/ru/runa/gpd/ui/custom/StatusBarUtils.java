package ru.runa.gpd.ui.custom;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class StatusBarUtils {

    public static void updateStatusBar(String message) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow == null || activeWindow.getActivePage() == null) {
            return;
        }
        IEditorPart editorPart = activeWindow.getActivePage().getActiveEditor();
        if (editorPart == null) {
            return;
        }
        IEditorSite editorSite = editorPart.getEditorSite();
        if (editorSite == null) {
            return;
        }
        final IStatusLineManager statusLineManager = editorSite.getActionBars().getStatusLineManager();
        statusLineManager.setMessage(message);
        PlatformUI.getWorkbench().getDisplay().timerExec(5000, new Runnable() {
            @Override
            public void run() {
                statusLineManager.setMessage(null);
            }
        });
    }

}

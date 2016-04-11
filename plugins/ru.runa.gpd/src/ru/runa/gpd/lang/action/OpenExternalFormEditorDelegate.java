package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PartInitException;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.Streamer;

public class OpenExternalFormEditorDelegate extends OpenFormEditorDelegate {
    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws PartInitException {
        String htmlEditorPath = Activator.getPrefString(PrefConstants.P_FORM_EXTERNAL_EDITOR_PATH);
        String filePath = file.getLocation().toOSString();
        try {
            String[] commands = { htmlEditorPath, filePath };
            Process process = Runtime.getRuntime().exec(commands);
            new ProcessListener(process, file).start();
            new Streamer(process.getErrorStream()).start();
            new Streamer(process.getInputStream()).start();
        } catch (Throwable e) {
            PluginLogger.logError("Failed to start program \n" + htmlEditorPath, e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        action.setEnabled(Activator.getPrefBoolean(PrefConstants.P_FORM_USE_EXTERNAL_EDITOR));
    }

    private class ProcessListener extends Thread {
        private final Process process;
        private final IFile file;

        public ProcessListener(Process process, IFile file) {
            this.process = process;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
                file.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }
}

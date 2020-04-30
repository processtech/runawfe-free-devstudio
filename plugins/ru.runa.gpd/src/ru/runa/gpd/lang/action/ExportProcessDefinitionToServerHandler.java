package ru.runa.gpd.lang.action;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.wizard.ExportParWizardPage.ParDeployOperation;
import ru.runa.gpd.util.IOUtils;

public class ExportProcessDefinitionToServerHandler extends AbstractHandler implements PrefConstants {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!Activator.getDefault().getPreferenceStore().getBoolean(P_ALLOW_UPDATE_LAST_VERSION_BY_KEYBINDING)) {
            updateStatusBar(Localization.getString("ExportProcessDefinitionToServerHandler.disabled"));
            return null;
        }
        saveDirtyEditors();
        IContainer parent = IOUtils.getCurrentFile().getParent();
        for (IFile file : ProcessCache.getAllProcessDefinitionsMap().keySet()) {
            if (!parent.equals(file.getParent())) {
                continue;
            }
            ProcessDefinition definition = ProcessCache.getProcessDefinition(file);
            if (definition != null && !(definition instanceof SubprocessDefinition)) {
                ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(file);
                if (!processDefinition.isInvalid()) {
                    try {
                        export(file);
                        updateStatusBar(Localization.getString("ExportProcessDefinitionToServerHandler.completed"));
                    } catch (Throwable th) {
                        PluginLogger.logError(Localization.getString("ExportParWizardPage.error.export"), th);
                    }
                } else {
                    Dialogs.error(Localization.getString("ExportProcessDefinitionToServerHandler.invalid.process.definition"));
                }
            }
        }
        return null;
    }

    private void export(IFile definitionFile) throws Exception {
        IFolder processFolder = (IFolder) definitionFile.getParent();
        processFolder.refreshLocal(IResource.DEPTH_ONE, null);
        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
        List<IFile> resourcesToExport = new ArrayList<IFile>();
        IResource[] members = processFolder.members();
        for (IResource resource : members) {
            if (resource instanceof IFile) {
                resourcesToExport.add((IFile) resource);
            }
        }
        new ParDeployOperation(resourcesToExport, definition.getName(), true).run(null);
    }

    private boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(false);
    }

    private void updateStatusBar(String message) {
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
            public void run() {
                statusLineManager.setMessage(null);
            }
        });
    }

}

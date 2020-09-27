package ru.runa.gpd.lang.action;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.StatusBarUtils;
import ru.runa.gpd.ui.wizard.ExportParWizardPage.ParDeployOperation;
import ru.runa.gpd.util.IOUtils;

public class ExportProcessDefinitionToServerHandler extends AbstractHandler implements PrefConstants {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!WfeServerConnector.getInstance().getSettings().isAllowUpdateLastVersionByKeyBinding()) {
            StatusBarUtils.updateStatusBar(Localization.getString("ExportProcessDefinitionToServerHandler.disabled") + " "
                    + WfeServerConnector.getInstance().getSettings().getUrl());
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
        new ParDeployOperation(resourcesToExport, definition.getName(), new ByteArrayOutputStream(), true).exportResources(null);
    }

    private boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(false);
    }

}

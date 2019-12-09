package ru.runa.gpd.lang.action;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.ProcessSaveHistory;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.view.ValidationErrorsView;
import ru.runa.gpd.ui.wizard.ExportParWizardPage.ParDeployOperation;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public class ExportProcessToServerHanlder extends AbstractHandler implements PrefConstants {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IFile currentFile = IOUtils.getCurrentFile();
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(currentFile);
        try {
            save(processDefinition, currentFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!processDefinition.isInvalid()) {
            export(currentFile);
        } else {
            Dialogs.error(Localization.getString("ExportParToServer.error"));
        }
        return null;
    }

    private void export(IFile definitionFile) {
        try {
            IFolder processFolder = (IFolder) definitionFile.getParent();
            processFolder.refreshLocal(IResource.DEPTH_ONE, null);
            ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
            int validationResult = ProcessDefinitionValidator.validateDefinition(definition);
            if (validationResult != 0) {
                Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ValidationErrorsView.ID);
                if (validationResult == 2) {
                    PluginLogger.logErrorWithoutDialog((Localization.getString("ExportParWizardPage.page.errorsExist")));
                    return;
                }
            }
            for (SubprocessDefinition subprocessDefinition : definition.getEmbeddedSubprocesses().values()) {
                validationResult = ProcessDefinitionValidator.validateDefinition(subprocessDefinition);
                if (validationResult != 0) {
                    if (validationResult == 2) {
                        PluginLogger.logErrorWithoutDialog((Localization.getString("ExportParWizardPage.page.errorsExistInEmbeddedSubprocess")));
                        return;
                    }
                }
            }
            definition.getLanguage().getSerializer().validateProcessDefinitionXML(definitionFile);
            List<IFile> resourcesToExport = new ArrayList<IFile>();
            IResource[] members = processFolder.members();
            for (IResource resource : members) {
                if (resource instanceof IFile) {
                    resourcesToExport.add((IFile) resource);
                }
            }
            boolean allowUpdate = Activator.getDefault().getPreferenceStore().getBoolean(P_ALLOW_UPDATE_LAST_VERSION_BY_KEYBINDING);
            new ParDeployOperation(resourcesToExport, definition.getName(), allowUpdate).run(null);
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(Localization.getString("ExportParWizardPage.error.export"), th);
        }
    }

    private void save(ProcessDefinition definition, IFile definitionFile) throws Exception {
        ProcessDefinitionValidator.validateDefinition(definition);
        WorkspaceOperations.saveProcessDefinition(definition);
        definition.setDirty(false);
        ProcessSaveHistory.addSavepoint(definitionFile);
    }
}

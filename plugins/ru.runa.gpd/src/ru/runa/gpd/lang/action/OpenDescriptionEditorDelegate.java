package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class OpenDescriptionEditorDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            String processId = processDefinition.getId();
            IFile file = null;
            if (processId == null) {
                file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
            } else {
                file = IOUtils.getAdjacentFile(getDefinitionFile(), processId + "." + ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
            }
            if (file == null || !file.exists()) {
                IOUtils.createFile(file);
            }
            IDE.openEditor(getWorkbenchPage(), file);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}

package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.FormNodeSetScriptFileCommand;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;

public class OpenFormScriptDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            if (!formNode.hasFormScript()) {
                setNewScriptFormFile(formNode, formNode.getId() + "." + FormNode.SCRIPT_SUFFIX);
            }
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), formNode.getScriptFileName());
            if (!file.exists()) {
                IOUtils.createFile(file, getClass().getResourceAsStream("/conf/form.template.js"));
            }
            IDE.openEditor(getWorkbenchPage(), file, true);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private void setNewScriptFormFile(FormNode formNode, String fileName) {
        FormNodeSetScriptFileCommand command = new FormNodeSetScriptFileCommand();
        command.setFormNode(formNode);
        command.setScriptFileName(fileName);
        executeCommand(command);
    }
}

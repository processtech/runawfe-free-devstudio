package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;

public abstract class OpenFormEditorDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            String fileName = formNode.getFormFileName();
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            openInEditor(file, formNode);
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    protected abstract void openInEditor(IFile file, FormNode formNode) throws CoreException;
}

package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.dialog.CopyFormDialog;
import ru.runa.gpd.util.IOUtils;

public class CopyFormDelegate extends FormDelegate {
    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            CopyFormDialog copyFormDialog = new CopyFormDialog(formNode);
            if (copyFormDialog.open() != Window.OK) {
                return;
            }
            FormNode sourceFormNode = copyFormDialog.getSelectedFormNode();
            if (!formNode.hasFormValidation() && sourceFormNode.hasFormValidation()) {
                String fileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
                IFile sourceFile = IOUtils.getAdjacentFile(getDefinitionFile(), sourceFormNode.getValidationFileName());
                IOUtils.copyFile(sourceFile.getContents(), file);
                setNewValidationFormFile(formNode, file.getName());
            }
            if (!formNode.hasForm() && sourceFormNode.hasForm()) {
                formNode.setFormType(sourceFormNode.getFormType());
                formNode.setTemplateFileName(sourceFormNode.getTemplateFileName());
                String fileName = formNode.getId().concat(".").concat(formNode.getFormType());
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
                IFile sourceFile = IOUtils.getAdjacentFile(getDefinitionFile(), sourceFormNode.getFormFileName());
                IOUtils.copyFile(sourceFile.getContents(), file);
                setNewFormFile(formNode, file.getName());
                openForm(action, "");
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
    }

}

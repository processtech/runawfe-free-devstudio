package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.dialog.CopyFormDialog;
import ru.runa.gpd.util.IOUtils;

public class CopyFormDelegate extends FormDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof StartState) {
                StartState startState = (StartState) element;
                if (startState.getProcessDefinition() instanceof SubprocessDefinition) {
                    action.setEnabled(false);
                    return;
                }
            }
        }
        action.setEnabled(true);
    }

    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            if (formNode instanceof StartState && formNode.getProcessDefinition() instanceof SubprocessDefinition) {
                Dialogs.error(Localization.getString("error.startState.formNotAllowedInEmbeddedSubprocess"));
                return;
            }
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
            }
            if (!formNode.hasFormScript() && sourceFormNode.hasFormScript()) {
                String fileName = formNode.getId().concat(".").concat(FormNode.SCRIPT_SUFFIX);
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
                IFile sourceFile = IOUtils.getAdjacentFile(getDefinitionFile(), sourceFormNode.getScriptFileName());
                IOUtils.copyFile(sourceFile.getContents(), file);
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
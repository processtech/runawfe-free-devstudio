package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.editor.gef.command.FormNodeSetFileCommand;
import ru.runa.gpd.editor.gef.command.FormNodeSetValidationFileCommand;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;
import ru.runa.gpd.ui.wizard.ValidatorWizard;
import ru.runa.gpd.validation.ValidatorDialog;

public abstract class FormDelegate extends BaseModelActionDelegate {

    protected void setNewFormFile(FormNode formNode, String fileName) {
        FormNodeSetFileCommand command = new FormNodeSetFileCommand();
        command.setFormNode(formNode);
        command.setFileName(fileName);
        executeCommand(command);
        // formNode.setDirty();
    }

    protected void openForm(IAction action, String editorType) {
        OpenFormEditorDelegate openFormEditorDelegate;
        if (ChooseFormTypeDialog.EDITOR_EXTERNAL.equals(editorType)) {
            openFormEditorDelegate = new OpenExternalFormEditorDelegate();
        } else {
            openFormEditorDelegate = new OpenVisualFormEditorDelegate();
        }
        initModelActionDelegate(openFormEditorDelegate);
        openFormEditorDelegate.run(action);
    }
    
    public void openValidationFile(FormNode formNode, IFile validationFile) {
        ValidatorWizard wizard = new ValidatorWizard(validationFile, formNode);
        ValidatorDialog dialog = new ValidatorDialog(wizard);
        if (dialog.open() == IDialogConstants.OK_ID) {
            formNode.setDirty();
        }
    }

    protected void setNewValidationFormFile(FormNode formNode, String fileName) {
        FormNodeSetValidationFileCommand command = new FormNodeSetValidationFileCommand();
        command.setFormNode(formNode);
        command.setValidationFileName(fileName);
        executeCommand(command);
    }

}

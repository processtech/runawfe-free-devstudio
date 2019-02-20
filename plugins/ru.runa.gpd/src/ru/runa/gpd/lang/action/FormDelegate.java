package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.gef.command.FormNodeSetFileCommand;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;

public abstract class FormDelegate extends BaseModelActionDelegate {

    protected void setNewFormFile(FormNode formNode, String fileName) {
        FormNodeSetFileCommand command = new FormNodeSetFileCommand();
        command.setFormNode(formNode);
        command.setFileName(fileName);
        executeCommand(command);
    }

    protected void openForm(IAction action, String editorType) {
        OpenFormEditorDelegate openFormEditorDelegate;
        if (ChooseFormTypeDialog.EDITOR_EXTERNAL.equals(editorType)) {
            openFormEditorDelegate = new OpenExternalFormEditorDelegate();
        } else {
            openFormEditorDelegate = new OpenJointFormEditorDelegate();
        }
        initModelActionDelegate(openFormEditorDelegate);
        openFormEditorDelegate.run(action);
    }
    
}

package ru.runa.gpd.form.jointeditor;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.action.OpenExternalFormEditorDelegate;
import ru.runa.gpd.lang.action.OpenFormEditorDelegate;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;

public class CreateFormDelegate extends ru.runa.gpd.lang.action.CreateFormDelegate {

    @Override
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

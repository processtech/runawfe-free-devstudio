package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;
import ru.runa.gpd.util.IOUtils;

public class CreateFormDelegate extends FormDelegate {
    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            ChooseFormTypeDialog chooseFormTypeDialog = new ChooseFormTypeDialog();
            if (chooseFormTypeDialog.open() != Window.OK) {
                return;
            }
            formNode.setFormType(chooseFormTypeDialog.getType());
            if (!FormTypeProvider.getFormType(formNode.getFormType()).isCreationAllowed()) {
                Dialogs.error(Localization.getString("FormType.creationNotAllowed"));
                return;
            }
            String fileName = formNode.getId().concat(".").concat(formNode.getFormType());
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            if (!file.exists()) {
                IOUtils.createFile(file);
            }
            setNewFormFile(formNode, fileName);
            openForm(action, chooseFormTypeDialog.getEditorType());
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
    }

}

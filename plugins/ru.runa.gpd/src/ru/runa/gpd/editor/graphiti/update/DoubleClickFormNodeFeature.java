package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.action.OpenExternalFormEditorDelegate;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;
import ru.runa.gpd.util.IOUtils;

public class DoubleClickFormNodeFeature extends DoubleClickElementFeature implements PrefConstants {

    @Override
    public boolean canExecute(ICustomContext context) {
        return fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement()) instanceof FormNode && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        FormNode formNode = (FormNode) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        try {
            if (formNode.hasForm()) {
                String fileName = formNode.getFormFileName();
                IFile file = IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), fileName);
                Activator.getDefault().getPreferenceStore().setValue(P_JOINT_FORM_EDITOR_SELECTED_PAGE, P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM);
                FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
            } else {
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
                IFile file = IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), fileName);
                if (!file.exists()) {
                    IOUtils.createFile(file);
                }
                formNode.setFormFileName(fileName);
                if (ChooseFormTypeDialog.EDITOR_EXTERNAL.equals(chooseFormTypeDialog.getEditorType())) {
                    new OpenExternalFormEditorDelegate().run(null);
                } else {
                    FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
    }

}

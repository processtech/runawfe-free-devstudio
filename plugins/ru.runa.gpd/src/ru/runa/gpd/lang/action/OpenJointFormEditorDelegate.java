package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.Activator;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;

public class OpenJointFormEditorDelegate extends OpenFormEditorDelegate implements PrefConstants {

    protected String getSelectedPagePreference() {
        return P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM;
    }

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws CoreException {
        Activator.getDefault().getPreferenceStore().setValue(P_JOINT_FORM_EDITOR_SELECTED_PAGE, getSelectedPagePreference());
        FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
    }

}

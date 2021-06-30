package ru.runa.gpd.formeditor.ftl.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.Activator;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.ValidationErrorDetails;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.IOUtils;

public class ComponentValidationErrorDetails implements ValidationErrorDetails {
    private final FormNode formNode;
    private final Component component;

    public ComponentValidationErrorDetails(FormNode formNode, Component component) {
        this.formNode = formNode;
        this.component = component;
    }

    @Override
    public void show() throws CoreException {
        if (formNode.hasForm()) {
            String fileName = formNode.getFormFileName();
            IFile file = IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), fileName);
            Activator.getDefault().getPreferenceStore().setValue(PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE,
                    PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM);
            ((FormEditor) FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode))
                    .selectComponent(component.getNumberOnFormValidation());
        }
    }

}

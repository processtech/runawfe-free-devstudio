package ru.runa.gpd.formeditor.ftl.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.Activator;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.ValidationErrorDetails;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.IOUtils;

public class DefaultParameterTypeValidator implements IParameterTypeValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        List<ValidationError> list = Lists.newArrayList();
        Object value = component.getParameterValue(parameter);
        if (parameter.isRequired()) {
            if (value instanceof String) {
                if (Strings.isNullOrEmpty((String) value)) {
                    list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                }
            } else {
                List<String> strings = (List<String>) value;
                if (strings.size() == 0) {
                    list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                }
                for (String string : strings) {
                    if (Strings.isNullOrEmpty(string)) {
                        list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                    }
                }
            }
        }
        return list;
    }

    private ValidationError createRequiredParameterIsNotSetError(FormNode formNode, Component component, ComponentParameter parameter) {
        return ValidationError.createError(formNode,
                Messages.getString("validation.requiredComponentParameterIsNotSet", component.getType().getLabel(), component.getId(),
                        parameter.getLabel()),
                new ValidationErrorDetails() {

                    @Override
                    public void show() throws CoreException {
                        if (formNode.hasForm()) {
                            String fileName = formNode.getFormFileName();
                            IFile file = IOUtils.getAdjacentFile(formNode.getProcessDefinition().getFile(), fileName);
                            Activator.getDefault().getPreferenceStore().setValue(PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE,
                                    PrefConstants.P_JOINT_FORM_EDITOR_SELECTED_PAGE_FORM);
                            // IEditorPart editorPart = FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
                            ((FormEditor) FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode))
                                    .selectComponent(component.getId());
                        }
                    }

                });
    }

}

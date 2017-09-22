package ru.runa.gpd.formeditor;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.TemplateProcessor;
import ru.runa.gpd.formeditor.ftl.conv.VariableSearchHashModel;
import ru.runa.gpd.formeditor.ftl.ui.FormComponentsView;
import ru.runa.gpd.formeditor.ftl.validation.IComponentValidator;
import ru.runa.gpd.formeditor.ftl.validation.ValidationHashModel;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.validation.FormNodeValidation;

public class FtlFormType extends BaseHtmlFormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FormComponentsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
        return super.openForm(formFile, formNode);
    }

    @Override
    protected Map<String, FormVariableAccess> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        VariableSearchHashModel model = new VariableSearchHashModel(formNode.getProcessDefinition());
        TemplateProcessor.process(formNode.getProcessDefinition().getName() + formNode.getId(), formBytes, model);
        return model.getUsedVariables();
    }

    @Override
    public void validate(FormNode formNode, byte[] formData, FormNodeValidation validation, List<ValidationError> errors) throws Exception {
        super.validate(formNode, formData, validation, errors);
        ValidationHashModel model = new ValidationHashModel(formNode.getProcessDefinition());
        TemplateProcessor.process(formNode.getProcessDefinition().getName() + formNode.getId(), formData, model);
        for (Component component : model.getComponents()) {
            IComponentValidator validator = component.getType().getValidator();
            List<ValidationError> list = validator.validate(formNode, component);
            errors.addAll(list);
        }
    }

}

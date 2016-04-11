package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.MultiTextEdit;

import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Objects;

public class FormNodePresentation extends VariableRenameProvider<FormNode> {
    private final IFolder folder;

    public FormNodePresentation(IFolder folder, FormNode formNode) {
        this.folder = folder;
        setElement(formNode);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        CompositeChange result = new CompositeChange(element.getName());
        if (element.hasForm()) {
            FormType formType = FormTypeProvider.getFormType(element.getFormType());
            IFile file = folder.getFile(element.getFormFileName());
            result.addAll(processFile(formType, file, Localization.getString("Search.formNode.form"), oldVariable.getName(), newVariable.getName()));
            if (element.hasFormValidation()) {
                file = folder.getFile(element.getValidationFileName());
                result.addAll(processFile(formType, file, Localization.getString("Search.formNode.validation"), oldVariable.getName(),
                        newVariable.getName()));
                if (!Objects.equal(oldVariable.getName(), oldVariable.getScriptingName())) {
                    result.addAll(processFile(formType, file, Localization.getString("Search.formNode.validation"), oldVariable.getScriptingName(),
                            newVariable.getScriptingName()));
                }
            }
        }
        if (result.getChildren().length > 0) {
            return Arrays.asList((Change) result);
        }
        return new ArrayList<Change>();
    }

    private Change[] processFile(FormType formType, IFile file, final String label, String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        MultiTextEdit multiEdit = formType.searchVariableReplacements(file, variableName, replacement);
        if (multiEdit.getChildrenSize() > 0) {
            TextFileChange fileChange = new TextFileChange(file.getName(), file) {
                @SuppressWarnings("rawtypes")
                @Override
                public Object getAdapter(Class adapter) {
                    if (adapter == TextEditChangeNode.class) {
                        return new ChangeNode(this, element, label);
                    }
                    return super.getAdapter(adapter);
                }
            };
            fileChange.setEdit(multiEdit);
            changes.add(fileChange);
        }
        return changes.toArray(new Change[changes.size()]);
    }
}

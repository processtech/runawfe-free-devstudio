package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

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
            IFile fileForm = folder.getFile(element.getFormFileName());
            String formLabel = Localization.getString("Search.formNode.form");
            result.addAll(processFile(formType, fileForm, formLabel, oldVariable, newVariable, false));
            if (element.hasFormValidation()) {
                IFile fileValidation = folder.getFile(element.getValidationFileName());
                String validationLabel = Localization.getString("Search.formNode.validation");
                result.addAll(processFile(formType, fileValidation, validationLabel, oldVariable, newVariable, true));
            }
        }
        if (result.getChildren().length > 0) {
            return Arrays.asList((Change) result);
        }
        return new ArrayList<Change>();
    }

    private Change[] processFile(FormType formType, IFile file, final String label, Variable oldVariable, Variable newVariable,
            boolean checkScriptingName) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        MultiTextEdit multiEdit = searchReplacements(formType, file, oldVariable.getName(), newVariable.getName(), oldVariable.getScriptingName(),
                newVariable.getScriptingName(), oldVariable, checkScriptingName);
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

    private MultiTextEdit searchReplacements(FormType formType, IFile file, String oldVariableName, String newVariableName,
            String oldVariableScriptingName, String newVariableScriptingName, Variable oldVariable, boolean checkScriptingName) throws Exception {
        MultiTextEdit multiEdit = formType.searchVariableReplacements(file, oldVariableName, newVariableName);
        if (checkScriptingName && !Objects.equal(oldVariableName, oldVariableScriptingName)) {
            MultiTextEdit multiEditScripting = formType.searchVariableReplacements(file, oldVariableScriptingName, newVariableScriptingName);
            addChild(multiEdit, multiEditScripting);
        }
        // complex type - search replacement for attributes
        VariableUserType type = oldVariable.getUserType();
        if (type != null) {
            List<Variable> attributes = type.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                Iterator<Variable> i = attributes.iterator();
                while (i.hasNext()) {
                    Variable attribute = i.next();
                    MultiTextEdit multiEditAttribute = searchReplacements(formType, file, oldVariableName + "." + attribute.getName(),
                            newVariableName + "." + attribute.getName(), oldVariableScriptingName + "." + attribute.getScriptingName(),
                            newVariableScriptingName + "." + attribute.getScriptingName(), attribute, checkScriptingName);
                    addChild(multiEdit, multiEditAttribute);
                }
            }
        }
        return multiEdit;
    }

    private void addChild(MultiTextEdit multiTextEdit1, MultiTextEdit multiTextEdit2) {
        if (multiTextEdit2.hasChildren()) {
            TextEdit[] children = multiTextEdit2.removeChildren();
            for (TextEdit child : children) {
                multiTextEdit1.addChild(child);
            }
        }
    }
}

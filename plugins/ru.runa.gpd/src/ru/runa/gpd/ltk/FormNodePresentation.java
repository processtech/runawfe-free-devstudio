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
    public List<Change> getChanges(Variable oldVar, Variable newVar) throws Exception {
        CompositeChange result = new CompositeChange(element.getName());
        if (element.hasForm()) {
            FormType formType = FormTypeProvider.getFormType(element.getFormType());
            IFile fileForm = folder.getFile(element.getFormFileName());
            String formLabel = Localization.getString("Search.formNode.form");
            result.addAll(processFile(formType, fileForm, formLabel, oldVar, newVar, false));
            if (element.hasFormValidation()) {
                IFile fileValidation = folder.getFile(element.getValidationFileName());
                String validationLabel = Localization.getString("Search.formNode.validation");
                result.addAll(processFile(formType, fileValidation, validationLabel, oldVar, newVar, true));
            }
        }
        if (result.getChildren().length > 0) {
            return Arrays.asList((Change) result);
        }
        return new ArrayList<Change>();
    }

    private Change[] processFile(FormType formType, IFile file, final String label, Variable oldVar, Variable newVar, boolean checkScriptName)
            throws Exception {
        List<Change> changes = new ArrayList<Change>();
        MultiTextEdit multiEdit = searchReplacements(formType, file, oldVar.getName(), newVar.getName(), oldVar.getScriptingName(),
                newVar.getScriptingName(), oldVar, checkScriptName);
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

    private MultiTextEdit searchReplacements(FormType formType, IFile file, String oldVarName, String newVarName, String oldVarScriptName,
            String newVarScriptName, Variable oldVar, boolean checkScriptName) throws Exception {
        MultiTextEdit multiEdit = formType.searchVariableReplacements(file, oldVarName, newVarName);
        if (checkScriptName && !Objects.equal(oldVarName, oldVarScriptName)) {
            MultiTextEdit multiEditScripting = formType.searchVariableReplacements(file, oldVarScriptName, newVarScriptName);
            if (multiEditScripting.hasChildren()) {
                multiEdit.addChild(multiEditScripting);
            }
        }
        // complex type - search replacement for attributes
        VariableUserType typ = oldVar.getUserType();
        if (typ != null) {
            List<Variable> attrs = typ.getAttributes();
            if (attrs != null && !attrs.isEmpty()) {
                Iterator<Variable> i = attrs.iterator();
                while (i.hasNext()) {
                    Variable attr = i.next();
                    MultiTextEdit multiEditAttr = searchReplacements(formType, file, oldVarName + "." + attr.getName(),
                            newVarName + "." + attr.getName(), oldVarScriptName + "." + attr.getScriptingName(),
                            newVarScriptName + "." + attr.getScriptingName(), attr, checkScriptName);
                    if (multiEditAttr.hasChildren()) {
                        multiEdit.addChild(multiEditAttr);
                    }
                }
            }
        }
        return multiEdit;
    }
}

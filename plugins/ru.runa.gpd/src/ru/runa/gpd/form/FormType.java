package ru.runa.gpd.form;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorPart;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.search.VariableSearchVisitor;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;

public abstract class FormType {

    private String type;
    private String name;
    private int order;

    /**
     * For deprecated form types.
     * 
     * @return
     */
    public boolean isCreationAllowed() {
        return true;
    }

    /**
     * Open form editor.
     */
    public abstract IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException;

    /**
     * Retrieve variables defined in form.
     */
    public abstract Map<String, FormVariableAccess> getFormVariableNames(FormNode formNode, byte[] formData) throws Exception;

    /**
     * Form validation.
     */
    public void validate(FormNode formNode, byte[] formData, FormNodeValidation validation, List<ValidationError> errors) throws Exception {
        Map<String, FormVariableAccess> formVariables = getFormVariableNames(formNode, formData);
        List<String> allVariableNames = formNode.getVariableNames(true);
        for (Map.Entry<String, FormVariableAccess> formEntry : formVariables.entrySet()) {
            switch (formEntry.getValue()) {
            case DOUBTFUL:
                errors.add(ValidationError.createLocalizedWarning(formNode, "formNode.formVariableTagUnknown", formEntry.getKey()));
                continue;
            case WRITE:
                if (!validation.getVariableNames().contains(formEntry.getKey())) {
                    errors.add(ValidationError.createLocalizedWarning(formNode, "formNode.formVariableOutOfValidation", formEntry.getKey()));
                }
                break;
            case READ:
                if (validation.getVariableNames().contains(formEntry.getKey())) {
                    errors.add(ValidationError.createLocalizedWarning(formNode, "formNode.formReadAccessVariableExistsInValidation",
                            formEntry.getKey()));
                }
                break;
            }
            if (!allVariableNames.contains(formEntry.getKey()) && formEntry.getValue() != FormVariableAccess.DOUBTFUL) {
                errors.add(ValidationError.createLocalizedError(formNode, "formNode.formVariableDoesNotExist", formEntry.getKey()));
            }
        }
        for (String validationVarName : validation.getVariableNames()) {
            if (!formVariables.keySet().contains(validationVarName)) {
                errors.add(ValidationError.createLocalizedWarning(formNode, "formNode.validationVariableOutOfForm", validationVarName));
            }
            if (!allVariableNames.contains(validationVarName)) {
                errors.add(ValidationError.createLocalizedError(formNode, "formNode.validationVariableDoesNotExist", validationVarName));
            }
        }
    }

    public MultiTextEdit searchVariableReplacements(IFile file, String variableName, String replacement) throws Exception {
        String text = IOUtils.readStream(file.getContents());
        Pattern pattern = Pattern.compile(Pattern.quote(variableName));
        Matcher matcher = pattern.matcher(text);
        MultiTextEdit multiEdit = new MultiTextEdit();
        int len = variableName.length();
        while (matcher.find()) {
            ReplaceEdit replaceEdit = new ReplaceEdit(matcher.start(), len, replacement);
            multiEdit.addChild(replaceEdit);
        }
        return multiEdit;
    }

    public MultiTextEdit searchVariableReplacementsInScript(IFile file, String variableName, String replacement) throws Exception {
        MultiTextEdit multiEdit = new MultiTextEdit();
        if (file.exists()) {
            String text = IOUtils.readStream(file.getContents());
            Pattern pattern = Pattern.compile(String.format(VariableSearchVisitor.REGEX_SCRIPT_VARIABLE, Pattern.quote(variableName)));
            Matcher matcher = pattern.matcher(text);
            int len = variableName.length();
            while (matcher.find()) {
                ReplaceEdit replaceEdit = new ReplaceEdit(matcher.start(), len, replacement);
                replaceEdit = new ReplaceEdit(replaceEdit.getOffset() + text.substring(replaceEdit.getOffset()).indexOf(variableName), len,
                        replacement);
                multiEdit.addChild(replaceEdit);
            }
        }
        return multiEdit;
    }

    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    void setOrder(int order) {
        this.order = order;
    }
}

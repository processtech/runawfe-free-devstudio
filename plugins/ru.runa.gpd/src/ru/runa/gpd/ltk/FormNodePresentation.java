package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorParser;

import com.google.common.base.Objects;

public class FormNodePresentation extends VariableRenameProvider<FormNode> {
    private final IFolder folder;

    public FormNodePresentation(IFolder folder, FormNode formNode) {
        this.folder = folder;
        setElement(formNode);
    }

    @Override
    public List<Change> getChanges(SortedMap<Variable, Variable> variablesMap) throws Exception {
        CompositeChange result = new CompositeChange(element.getName());
        if (element.hasForm()) {
            FormType formType = FormTypeProvider.getFormType(element.getFormType());
            IFile formFile = folder.getFile(element.getFormFileName());
            String formLabel = Localization.getString("Search.formNode.form");
            result.addAll(textEditToChangeArray(formFile, formLabel, processFile(formType, formFile, variablesMap, true)));
            if (element.hasFormValidation()) {
                IFile validationFile = folder.getFile(element.getValidationFileName());
                MultiTextEdit multiEdit = processFile(formType, validationFile, variablesMap, true);
                processGlobalValidators(validationFile, variablesMap, multiEdit);
                String validationLabel = Localization.getString("Search.formNode.validation");
                result.addAll(textEditToChangeArray(validationFile, validationLabel, multiEdit));
            }
            if (element.hasFormScript()) {
                // TODO
            }
        }
        if (element.hasFormScript()) {
            FormType formType = FormTypeProvider.getFormType(element.getFormType());
            IFile scriptFile = folder.getFile(element.getScriptFileName());
            String scriptLabel = Localization.getString("Search.formNode.script");
            result.addAll(textEditToChangeArray(scriptFile, scriptLabel, processScriptFile(formType, scriptFile, variablesMap)));
        }
        if (result.getChildren().length > 0) {
            return Arrays.asList((Change) result);
        }
        return new ArrayList<Change>();
    }

    /**
     * Rename variables in file (form or validation)
     */
    private MultiTextEdit processFile(FormType formType, IFile file, SortedMap<Variable, Variable> variablesMap, boolean checkScriptingName)
            throws Exception {
        MultiTextEdit multiEdit = new MultiTextEdit();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            addChildEdit(multiEdit, formType.searchVariableReplacements(file, oldVariable.getName(), newVariable.getName()));
            if (checkScriptingName && !Objects.equal(oldVariable.getName(), oldVariable.getScriptingName())) {
                addChildEdit(multiEdit, formType.searchVariableReplacements(file, oldVariable.getScriptingName(), newVariable.getScriptingName()));
            }
        }
        return multiEdit;
    }

    /**
     * Rename variables in script file
     */
    private MultiTextEdit processScriptFile(FormType formType, IFile file, SortedMap<Variable, Variable> variablesMap)
            throws Exception {
        MultiTextEdit multiEdit = new MultiTextEdit();
        for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
            Variable oldVariable = entry.getKey();
            Variable newVariable = entry.getValue();
            addChildEdit(multiEdit, formType.searchVariableReplacementsInScript(file, oldVariable.getScriptingName(), newVariable.getScriptingName()));
            if (!oldVariable.getScriptingName().equals(oldVariable.getName())) {
                addChildEdit(multiEdit, formType.searchVariableReplacementsInScript(file, oldVariable.getName(), newVariable.getName()));
            }
        }
        return multiEdit;
    }

    /**
     * Rename variables in global validator config (no quotes - use pattern)
     */
    private void processGlobalValidators(IFile file, SortedMap<Variable, Variable> variablesMap, MultiTextEdit multiEdit) throws Exception {
        FormNodeValidation validation = ValidatorParser.parseValidation(file);
        String text = IOUtils.readStream(file.getContents());
        for (ValidatorConfig globalConfig : validation.getGlobalConfigs()) {
            String groovyCode = globalConfig.getParams().get(ValidatorDefinition.EXPRESSION_PARAM_NAME);
            Pattern pattern = Pattern.compile(Pattern.quote(groovyCode));
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                for (Entry<Variable, Variable> entry : variablesMap.entrySet()) {
                    Variable oldVariable = entry.getKey();
                    Variable newVariable = entry.getValue();
                    replaceVariableNameInGroovyScript(groovyCode, oldVariable.getName(), newVariable.getName(), matcher.start(), multiEdit);
                    if (!Objects.equal(oldVariable.getName(), oldVariable.getScriptingName())) {
                        replaceVariableNameInGroovyScript(groovyCode, oldVariable.getScriptingName(), newVariable.getScriptingName(),
                                matcher.start(), multiEdit);
                    }
                }
            }
        }
    }

    private void replaceVariableNameInGroovyScript(String script, String variableName, String replacement, int startPosition, MultiTextEdit multiEdit) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(variableName) + "|[!\\s(]" + Pattern.quote(variableName));
        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            // not replace first char in match - !, \s, (
            int i = matcher.group().length() - variableName.length();
            addChildEdit(multiEdit, new ReplaceEdit(startPosition + matcher.start() + i, matcher.group().length() - i, replacement));
        }
    }

    /**
     * Transform MultiTextEdit to file changes array
     */
    private Change[] textEditToChangeArray(IFile file, final String label, MultiTextEdit multiEdit) {
        List<Change> changes = new ArrayList<Change>();
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

    /**
     * Add content of second TextEdit to first MultiTextEdit
     */
    private void addChildEdit(MultiTextEdit multiTextEdit1, TextEdit textEdit2) {
        TextEdit[] children = null;
        if (textEdit2 instanceof MultiTextEdit) {
            if (textEdit2.hasChildren()) {
                children = textEdit2.removeChildren();
            }
        } else if (textEdit2 instanceof ReplaceEdit) {
            children = new TextEdit[] { textEdit2 };
        }
        if (children != null) {
            for (TextEdit child : children) {
                boolean addChild = true;
                for (TextEdit textEdit : multiTextEdit1.getChildren()) {
                    if (textEdit.getOffset() == child.getOffset()) {
                        if (child.getLength() > textEdit.getLength()) {
                            multiTextEdit1.removeChild(textEdit);
                        } else {
                            addChild = false;
                            break;
                        }
                    }
                }
                if (addChild) {
                    multiTextEdit1.addChild(child);
                }
            }
        }
    }
}

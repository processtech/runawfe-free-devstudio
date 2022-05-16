package ru.runa.gpd.extension.businessRule;

import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.search.VariableSearchVisitor;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class BusinessRuleProvider extends DelegableProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        BusinessRuleEditorDialog dialog = new BusinessRuleEditorDialog(definition, delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) {
        String configuration = delegable.getDelegationConfiguration();
        if (configuration.trim().length() == 0) {
            errors.add(ValidationError.createLocalizedError((GraphElement) delegable, "delegable.invalidConfiguration.empty"));
        } else {
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            shell.parse(configuration);
        }
        return true;
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) throws Exception {
        List<Variable> variables = ((GraphElement) delegable).getProcessDefinition().getVariables(true, true);
        List<String> result = Lists.newArrayList();
        String configuration = "(" + delegable.getDelegationConfiguration() + ")";
        for (Variable variable : variables) {
            String variableName = String.format(VariableSearchVisitor.REGEX_SCRIPT_VARIABLE, variable.getScriptingName());
            if (Pattern.compile(variableName).matcher(configuration).find()) {
                result.add(variable.getName());
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        return delegable.getDelegationConfiguration().replaceAll(Pattern.quote(currentVariable.getScriptingName()),
                Matcher.quoteReplacement(previewVariable.getScriptingName()));
    }

}

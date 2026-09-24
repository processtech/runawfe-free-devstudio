package ru.runa.gpd.extension;

import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.search.VariableSearchVisitor;

public class GroovyBasedProvider extends DelegableProvider {

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) {
        String configuration = delegable.getDelegationConfiguration();
        if (configuration.trim().isEmpty()) {
            errors.add(ValidationError.createLocalizedError(delegable instanceof GraphElementAware
                    ? ((GraphElementAware) delegable).getGraphElement()
                    : ((GraphElement) delegable), "delegable.invalidConfiguration.empty"));
        } else {
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            shell.parse(configuration);
        }
        return true;
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) throws Exception {
        List<Variable> variables = ((ProcessDefinitionAware) delegable)
                .getProcessDefinition().getVariables(true, true);
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

package ru.runa.gpd.extension.decision;

import com.google.common.collect.Lists;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.search.VariableSearchVisitor;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class GroovyDecisionProvider extends DelegableProvider implements IDecisionProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        if (!HandlerArtifact.DECISION.equals(delegable.getDelegationType())) {
            throw new IllegalArgumentException("For decision handler only");
        }
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        List<Transition> transitions = ((Decision) delegable).getLeavingTransitions();
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : transitions) {
            transitionNames.add(transition.getName());
        }
        GroovyEditorDialog dialog = new GroovyEditorDialog(definition, transitionNames, delegable.getDelegationConfiguration());
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
    public Collection<String> getTransitionNames(Decision decision) {
        Optional<GroovyDecisionModel> model = GroovyCodeParser.parseDecisionModel(decision);
        if (model.isPresent()) {
            return model.get().getTransitionNames();
        }
        return null;
    }

    @Override
    public String getDefaultTransitionName(Decision decision) {
        Optional<GroovyDecisionModel> model = GroovyCodeParser.parseDecisionModel(decision);
        if (model.isPresent()) {
            return model.get().getDefaultTransitionName();
        }
        return null;
    }

    @Override
    public void transitionRenamed(Decision decision, String oldName, String newName) {
        String conf = decision.getDelegationConfiguration();
        conf = conf.replaceAll(Pattern.quote("\"" + oldName + "\""), Matcher.quoteReplacement("\"" + newName + "\""));
        decision.setDelegationConfiguration(conf);
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

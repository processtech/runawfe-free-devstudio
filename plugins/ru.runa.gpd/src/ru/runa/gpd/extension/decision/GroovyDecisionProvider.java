package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.extension.GroovyBasedProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class GroovyDecisionProvider extends GroovyBasedProvider implements IDecisionProvider {
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
}

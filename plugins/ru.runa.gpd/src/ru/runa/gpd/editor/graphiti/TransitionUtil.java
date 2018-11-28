package ru.runa.gpd.editor.graphiti;

import com.google.common.base.Strings;
import java.util.List;
import ru.runa.gpd.extension.decision.GroovyDecisionModel;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;

public abstract class TransitionUtil {

    public static void setDefaultFlow(ExclusiveGateway eg, String newConfiguration) {
        List<Transition> leavingTransitions = eg.getLeavingTransitions();
        if (leavingTransitions.size() > 1 && !Strings.isNullOrEmpty(newConfiguration)) {
            try {
                String defaultTransitionName = new GroovyDecisionModel(newConfiguration, eg.getProcessDefinition().getVariables(true, true))
                        .getDefaultTransitionName();
                eg.getLeavingTransitions().stream().forEach(t -> t.setDefaultFlow(defaultTransitionName.equals(t.getName())));
            } catch (Exception e) {
                // do nothing
            }
        } else {
            leavingTransitions.stream().forEach(t -> t.setDefaultFlow(false));
        }
    }

}

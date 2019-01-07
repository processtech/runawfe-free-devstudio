package ru.runa.gpd.editor.graphiti;

import com.google.common.base.Strings;
import java.util.List;
import ru.runa.gpd.Activator;
import ru.runa.gpd.extension.decision.GroovyDecisionModel;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;

public abstract class TransitionUtil {

    private final static String exclusiveGateway = "exclusiveGateway";
    private final static String asterisk = "*";

    public static boolean markDefaultTransition() {
        return Activator
                .getPrefBoolean(LanguageElementPreferenceNode.getBpmnPropertyName(exclusiveGateway, PrefConstants.P_BPMN_MARK_DEFAULT_TRANSITION));
    }

    public static void setDefaultFlow(ExclusiveGateway eg, String newConfiguration) {
        List<Transition> leavingTransitions = eg.getLeavingTransitions();
        if (leavingTransitions.size() > 1 && !Strings.isNullOrEmpty(newConfiguration)) {
            boolean defaultTransition = false;
            String defaultTransitionNames = Activator.getPrefString(
                    LanguageElementPreferenceNode.getBpmnPropertyName(exclusiveGateway, PrefConstants.P_BPMN_DEFAULT_TRANSITION_NAMES));
            if (!Strings.isNullOrEmpty(defaultTransitionNames)) {
                String[] names = defaultTransitionNames.split(";");
                nextTransition:
                for (Transition transition : leavingTransitions) {
                    if (defaultTransition) {
                        transition.setDefaultFlow(false);
                        continue nextTransition;
                    } else {
                        String transitionName = transition.getName();
                        for (String name : names) {
                            if (!Strings.isNullOrEmpty(name)) {
                                int nameLength = name.length();
                                if (name.startsWith(asterisk)) {
                                    if (name.endsWith(asterisk)) {
                                        if (nameLength > 2) {
                                            defaultTransition = transitionName.contains(name.substring(1, nameLength - 1));
                                        }
                                    } else {
                                        if (nameLength > 1) {
                                            defaultTransition = transitionName.endsWith(name.substring(1));
                                        }
                                    }
                                } else if (name.endsWith(asterisk)) {
                                    if (nameLength > 2) {
                                        defaultTransition = transitionName.startsWith(name.substring(0, nameLength - 1));
                                    }
                                } else {
                                    defaultTransition = transitionName.equals(name);
                                }
                                transition.setDefaultFlow(defaultTransition);
                                if (defaultTransition) {
                                    continue nextTransition;
                                }
                            }
                        }
                    }
                }
            }
            if (!defaultTransition) {
                try {
                    String defaultTransitionName = new GroovyDecisionModel(newConfiguration, eg.getProcessDefinition().getVariables(true, true))
                            .getDefaultTransitionName();
                    leavingTransitions.stream().forEach(t -> t.setDefaultFlow(defaultTransitionName.equals(t.getName())));
                } catch (Exception e) {
                    // do nothing
                }
            }
        } else {
            leavingTransitions.stream().forEach(t -> t.setDefaultFlow(false));
        }
    }

}

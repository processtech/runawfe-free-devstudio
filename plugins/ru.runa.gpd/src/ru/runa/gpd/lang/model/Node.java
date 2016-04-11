package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public abstract class Node extends NamedGraphElement implements Describable {
    private boolean minimizedView = false;

    public boolean isMinimizedView() {
        return minimizedView;
    }

    public void setMinimizedView(boolean minimazedView) {
        this.minimizedView = minimazedView;
        firePropertyChange(PROPERTY_MINIMAZED_VIEW, !minimizedView, minimizedView);
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("minimizedView".equals(name)) {
            return Objects.equal(value, isMinimizedView());
        }
        return super.testAttribute(target, name, value);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            return ((ITimed) this).getTimer().getDelay();
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return ((ITimed) this).getTimer().getAction();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            ((ITimed) this).getTimer().setDelay((Duration) value);
        } else if (PROPERTY_TIMER_ACTION.equals(id)) {
            ((ITimed) this).getTimer().setAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public void removeChild(GraphElement child) {
        super.removeChild(child);
        if (child instanceof Timer) {
            Transition timeoutTransition = getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME);
            if (timeoutTransition != null) {
                removeLeavingTransition(timeoutTransition);
            }
        }
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (!(this instanceof StartState) && !(this instanceof Timer && getParent() instanceof ITimed)) {
            if (getArrivingTransitions().size() == 0) {
                errors.add(ValidationError.createLocalizedError(this, "noInputTransitions"));
            }
        }
        if (!(this instanceof EndState) && !(this instanceof EndTokenState)) {
            if (getLeavingTransitions().size() == 0) {
                if (this instanceof Timer) {
                    // for jpdl
                    return;
                }
                errors.add(ValidationError.createLocalizedError(this, "noOutputTransitions"));
            }
        }
        List<Transition> transitions = getLeavingTransitions();
        for (Timer timer : getChildren(Timer.class)) {
            transitions.addAll(timer.getLeavingTransitions());
        }
        Set<String> transitionNames = Sets.newHashSet();
        for (Transition transition : transitions) {
            transitionNames.add(transition.getName());
        }
        if (transitionNames.size() != transitions.size()) {
            errors.add(ValidationError.createLocalizedError(this, "duplicatedTransitionNames"));
        }
    }

    public String getNextTransitionName(NodeTypeDefinition typeDefinition) {
        int runner = 1;
        String pattern = typeDefinition.getNamePattern(getProcessDefinition().getLanguage());
        while (true) {
            String candidate = pattern + runner;
            if (getTransitionByName(candidate) == null) {
                return candidate;
            }
            runner++;
        }
    }

    public Transition getTransitionByName(String name) {
        List<Transition> transitions = getLeavingTransitions();
        for (Transition transition : transitions) {
            if (name.equals(transition.getName())) {
                return transition;
            }
        }
        return null;
    }

    public void addLeavingTransition(Transition transition) {
        boolean renameAfterAddition = getTransitionByName(transition.getName()) != null;
        addChild(transition);
        if (renameAfterAddition) {
            transition.setName(getNextTransitionName(transition.getTypeDefinition()));
        }
        onLeavingTransitionAdded(transition);
    }

    public void onLeavingTransitionAdded(Transition transition) {
        firePropertyChange(NODE_LEAVING_TRANSITION_ADDED, null, transition);
        Node target = transition.getTarget();
        if (target != null) {
            target.firePropertyChange(NODE_ARRIVING_TRANSITION_ADDED, null, transition);
        }
        updateLeavingTransitions();
    }

    public void removeLeavingTransition(Transition transition) {
        removeChild(transition);
        firePropertyChange(NODE_LEAVING_TRANSITION_REMOVED, null, transition);
        Node target = transition.getTarget();
        if (target != null) {
            target.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, transition);
        }
        updateLeavingTransitions();
    }

    private void updateLeavingTransitions() {
        if (isExclusive()) {
            boolean exclusiveFlow = getLeavingTransitions().size() > 1;
            for (Transition leavingTransition : getLeavingTransitions()) {
                leavingTransition.setExclusiveFlow(exclusiveFlow);
            }
        }
    }

    public List<Transition> getLeavingTransitions() {
        return getChildren(Transition.class);
    }

    public List<Transition> getArrivingTransitions() {
        List<Transition> arrivingTransitions = new ArrayList<Transition>();
        List<Node> allNodes = getProcessDefinition().getNodesRecursive();
        for (Node node : allNodes) {
            List<Transition> leaving = node.getLeavingTransitions();
            for (Transition transition : leaving) {
                if (this.equals(transition.getTarget())) {
                    arrivingTransitions.add(transition);
                }
            }
        }
        return arrivingTransitions;
    }

    public final boolean canAddArrivingTransition(Node source) {
        List<Transition> transitions = getArrivingTransitions();
        return allowArrivingTransition(source, transitions);
    }

    public final boolean canReconnectArrivingTransition(Transition transition, Node source) {
        List<Transition> transitions = getArrivingTransitions();
        transitions.remove(transition);
        return allowArrivingTransition(source, transitions);
    }

    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        if (this.equals(source)) {
            // Disable self referencing
            return false;
        }
        return true;
    }

    public final boolean canReconnectLeavingTransition(Transition transition, Node target) {
        List<Transition> transitions = getLeavingTransitions();
        transitions.remove(transition);
        return allowLeavingTransition(transitions);
    }

    public final boolean canAddLeavingTransition() {
        List<Transition> transitions = getLeavingTransitions();
        return allowLeavingTransition(transitions);
    }

    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return true;
    }

    public boolean isExclusive() {
        return false;
    }

    @Override
    public Node getCopy(GraphElement parent) {
        Node copy = (Node) super.getCopy(parent);
        copy.setMinimizedView(isMinimizedView());
        if (this instanceof ITimed) {
            Timer timer = ((ITimed) this).getTimer();
            if (timer != null) {
                timer.getCopy(copy);
            }
        }
        return copy;
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        if (this instanceof ITimed) {
            Timer timer = ((ITimed) this).getTimer();
            if (timer != null) {
                result.addAll(timer.getUsedVariables(processFolder));
            }
        }
        return result;
    }
}

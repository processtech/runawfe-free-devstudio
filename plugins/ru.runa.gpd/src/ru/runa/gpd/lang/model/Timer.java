package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.TimerActionPropertyDescriptor;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableUtils;

public class Timer extends Node {
    private Duration duration = new Duration();
    private TimerAction action;

    public Timer() {
        super();
        nodeRegulationsProperties.setIsEnabled(false);
    }

    public Duration getDelay() {
        return duration;
    }

    public void setDelay(Duration duration) {
        Duration old = this.duration;
        this.duration = duration;
        firePropertyChange(PROPERTY_TIMER_DELAY, old, duration);
        if (getParent() != null) {
            getParent().firePropertyChange(PROPERTY_TIMER_DELAY, old, duration);
        }
    }

    public TimerAction getAction() {
        return action;
    }

    public void setAction(TimerAction timerAction) {
        TimerAction old = this.action;
        this.action = timerAction;
        firePropertyChange(PROPERTY_TIMER_ACTION, old, action);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (duration.getVariableName() != null && !getProcessDefinition().getVariableNames(false).contains(duration.getVariableName())) {
            errors.add(ValidationError.createLocalizedError(this, "timerState.invalidVariable"));
        }
        if (action != null) {
            action.validate(errors, definitionFile);
            if (!getLeavingTransitions().isEmpty() && action.getRepeatDelay().hasDuration()) {
                errors.add(ValidationError.createLocalizedWarning(this, "timerState.action.repeatWithTransition.notSupported"));
            }
        }
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new DurationPropertyDescriptor(PROPERTY_TIMER_DELAY, getProcessDefinition(), getDelay(), Localization
                .getString("property.duration")));
        descriptors.add(new TimerActionPropertyDescriptor(PROPERTY_TIMER_ACTION, Localization.getString("Timer.action"), this));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TIMER_DELAY.equals(id)) {
            return duration;
        }
        if (PROPERTY_TIMER_ACTION.equals(id)) {
            return action;
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
            setDelay((Duration) value);
        } else if (PROPERTY_TIMER_ACTION.equals(id)) {
            setAction((TimerAction) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public String getNextTransitionName(NodeTypeDefinition typeDefinition) {
        return PluginConstants.TIMER_TRANSITION_NAME;
    }

    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        if (getParent() instanceof ITimed) {
            // boundary timer
            return false;
        }
        return super.allowArrivingTransition(source, transitions);
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return transitions.size() == 0;
    }

    @Override
    public Timer getCopy(GraphElement parent) {
        Timer copy = (Timer) super.getCopy(parent);
        if (getAction() != null) {
            copy.setAction(getAction().getCopy(parent.getProcessDefinition()));
        }
        if (getDelay() != null) {
            copy.setDelay(new Duration(getDelay()));
        }
        return copy;
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        if (duration != null && duration.getVariableName() != null) {
            Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), duration.getVariableName());
            if (variable != null) {
                result.add(variable);
            }
        }
        return result;
    }

    public void setDelayVariableName(String variableName) {
        Duration newDuration = new Duration(duration);
        newDuration.setVariableName(variableName);
        setDelay(newDuration);
    }
}

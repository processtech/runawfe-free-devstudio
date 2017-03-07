package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.Duration;
import ru.runa.wfe.extension.handler.EscalationActionHandler;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class TimerAction extends Action {
    private Duration repeatDelay = new Duration();
    private final ProcessDefinition processDefinition;

    public TimerAction(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public Duration getRepeatDelay() {
        return repeatDelay;
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setRepeatDuration(String duration) {
        if (!Strings.isNullOrEmpty(duration)) {
            this.repeatDelay = new Duration(duration);
        }
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(getDelegationClassName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDelegationClassName(), getDelegationConfiguration(), repeatDelay);
    }
    
    @Override
    public boolean equals(Object obj) {
        TimerAction t = (TimerAction) obj;
        return Objects.equal(getDelegationClassName(), t.getDelegationClassName()) && 
                Objects.equal(getDelegationConfiguration(), t.getDelegationConfiguration()) && 
                Objects.equal(repeatDelay, t.repeatDelay);
    }
    
    @Override
    public String toString() {
        if (Strings.isNullOrEmpty(getDelegationClassName())) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        if (EscalationActionHandler.class.getName().equals(getDelegationClassName())) {
            if (Strings.isNullOrEmpty(getDelegationConfiguration())) {
                buffer.append(Localization.getString("Variable.property.defaultValue"));
            } else {
                buffer.append(getDelegationConfiguration());
            }
        } else {
            buffer.append(getDelegationClassName());
        }
        buffer.append(" | ");
        buffer.append(repeatDelay.hasDuration() ? repeatDelay : Localization.getString("duration.norepeat"));
        return buffer.toString();
    }
    
    @Override
    public TimerAction getCopy(GraphElement parent) {
        TimerAction copy = new TimerAction((ProcessDefinition) parent);
        copy.setDescription(getDescription());
        copy.setDelegationClassName(getDelegationClassName());
        copy.setDelegationConfiguration(getDelegationConfiguration());
        copy.setRepeatDuration(getRepeatDelay().getDuration());
        return copy;
    }

}

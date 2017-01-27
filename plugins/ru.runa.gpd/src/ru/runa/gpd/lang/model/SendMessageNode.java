package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.util.Duration;

public class SendMessageNode extends MessagingNode implements Active {
    private Duration ttlDuration = new Duration("1 days");

    public Duration getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(Duration ttlDuration) {
        Duration old = this.ttlDuration;
        this.ttlDuration = ttlDuration;
        firePropertyChange(PROPERTY_TTL, old, ttlDuration);
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new DurationPropertyDescriptor(PROPERTY_TTL, getProcessDefinition(), getTtlDuration(), Localization
                .getString("property.message.ttl")));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TTL.equals(id)) {
            return ttlDuration;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TTL.equals(id)) {
            setTtlDuration((Duration) value);
            return;
        }
        super.setPropertyValue(id, value);
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    @Override
    public MessagingNode getCopy(GraphElement parent) {
        SendMessageNode copy = (SendMessageNode) super.getCopy(parent);
        copy.setTtlDuration(getTtlDuration());
        return copy;
    }

}

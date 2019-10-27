package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class AbstractEventNode extends MessageNode {
    private static String[] EVENT_NODE_TYPE_NAMES;
    private EventNodeType eventNodeType;

    static {
        List<String> eventNodeTypeNames = Lists.newArrayList();
        for (EventNodeType eventNodeType : EventNodeType.values()) {
            eventNodeTypeNames.add(Localization.getString("event.node.type." + eventNodeType.name().toLowerCase()));
        }
        EVENT_NODE_TYPE_NAMES = eventNodeTypeNames.toArray(new String[eventNodeTypeNames.size()]);
    }

    @Override
    public void setParent(GraphElement parent) {
        super.setParent(parent);
        if (parent instanceof ProcessDefinition) {
            eventNodeType = EventNodeType.message;
        } else {
            eventNodeType = EventNodeType.signal;
        }
    }

    public EventNodeType getEventNodeType() {
        return eventNodeType;
    }

    public void setEventNodeType(EventNodeType eventNodeType) {
        if (eventNodeType != this.eventNodeType) {
            EventNodeType old = this.eventNodeType;
            this.eventNodeType = eventNodeType;
            firePropertyChange(PROPERTY_EVENT_TYPE, old, this.eventNodeType);
        }
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_EVENT_TYPE, Localization.getString("property.eventType"), EVENT_NODE_TYPE_NAMES));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            if (eventNodeType == null) {
                return new Integer(-1);
            }
            return eventNodeType.ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            int index = ((Integer) value).intValue();
            setEventNodeType(EventNodeType.values()[index]);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public AbstractEventNode makeCopy(GraphElement parent) {
        AbstractEventNode copy = (AbstractEventNode) super.makeCopy(parent);
        copy.setParentContainer(parent);
        return copy;
    }

    @Override
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        ((AbstractEventNode) copy).setEventNodeType(getEventNodeType());
    }

}

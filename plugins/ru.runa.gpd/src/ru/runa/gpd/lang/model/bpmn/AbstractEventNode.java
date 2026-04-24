package ru.runa.gpd.lang.model.bpmn;

import java.util.Arrays;
import java.util.List;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.change.ChangeEventTypeFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MessageNode;

public class AbstractEventNode extends MessageNode {
    private EventNodeType eventNodeType = EventNodeType.message;

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
        descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_EVENT_TYPE, Localization.getString("property.eventType"), getEventNodeTypeNames()));
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
             UndoRedoUtil.executeFeature(new ChangeEventTypeFeature(this,EventNodeType.values()[index]));
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public AbstractEventNode makeCopy(GraphElement parent) {
        AbstractEventNode copy = (AbstractEventNode) super.makeCopy(parent);
        copy.setUiParentContainer(parent);
        return copy;
    }

    @Override
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        ((AbstractEventNode) copy).setEventNodeType(getEventNodeType());
    }

    private String[] getEventNodeTypeNames() {
        return Arrays.stream(EventNodeType.getSupportedTypes(this))
                .map(EventNodeType::getLabel)
                .toArray(String[]::new);
    }

}

package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.lang.model.AbstractTransition;

public class DottedTransition extends AbstractTransition {
    
    private EventNodeType eventNodeType;

    public void setEventNodeType(EventNodeType eventNodeType) {
        this.eventNodeType = eventNodeType;
    }

    public EventNodeType getEventNodeType() {
        return eventNodeType;
    }

    @Override
    public String getLabel() {
        return getName();
    }

}

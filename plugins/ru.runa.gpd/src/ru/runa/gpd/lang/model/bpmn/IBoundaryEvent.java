package ru.runa.gpd.lang.model.bpmn;

public interface IBoundaryEvent {

    public boolean isInterruptingBoundaryEvent();

    public void setInterruptingBoundaryEvent(boolean interruptingBoundaryEvent);

}

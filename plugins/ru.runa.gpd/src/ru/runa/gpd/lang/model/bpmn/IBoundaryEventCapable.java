package ru.runa.gpd.lang.model.bpmn;

public interface IBoundaryEventCapable {

    public boolean isBoundaryEvent();

    public void updateBoundaryEventConstraint();

    public boolean isInterruptingBoundaryEvent();

    public void setInterruptingBoundaryEvent(boolean interruptingBoundaryEvent);

}

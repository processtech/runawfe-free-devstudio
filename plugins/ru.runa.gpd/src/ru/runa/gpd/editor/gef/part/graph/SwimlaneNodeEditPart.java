package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class SwimlaneNodeEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public SwimlanedNode getModel() {
        return (SwimlanedNode) super.getModel();
    }

    private Swimlane getSwimlane() {
        return getModel().getSwimlane();
    }

    @Override
    public void activate() {
        if (!isActive()) {
            Swimlane swimlane = getSwimlane();
            if (swimlane != null) {
                swimlane.addPropertyChangeListener(this);
            }
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            Swimlane swimlane = getSwimlane();
            if (swimlane != null) {
                swimlane.removePropertyChangeListener(this);
            }
            super.deactivate();
        }
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_SWIMLANE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (PROPERTY_SWIMLANE.equals(event.getPropertyName())) {
            if (event.getOldValue() instanceof Swimlane) {
                ((Swimlane) event.getOldValue()).removePropertyChangeListener(this);
            }
            if (event.getNewValue() instanceof Swimlane) {
                ((Swimlane) event.getNewValue()).addPropertyChangeListener(this);
            }
        }
    }
}

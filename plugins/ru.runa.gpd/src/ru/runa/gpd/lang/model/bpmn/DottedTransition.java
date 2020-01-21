package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;

public class DottedTransition extends NamedGraphElement {
    private Node target;
    private List<Point> bendpoints = Lists.newArrayList();

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        Node old = this.target;
        this.target = target;
        if (old != null) {
            old.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, this);
        }
        if (this.target != null) {
            this.target.firePropertyChange(NODE_ARRIVING_TRANSITION_ADDED, null, this);
        }
    }

    public Node getSource() {
        return (Node) getParent();
    }

    public List<Point> getBendpoints() {
        return bendpoints;
    }

    public void setBendpoints(List<Point> bendpoints) {
        this.bendpoints = bendpoints;
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, 1);
    }

    public void addBendpoint(int index, Point bendpoint) {
        getBendpoints().add(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void removeBendpoint(int index) {
        getBendpoints().remove(index);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void setBendpoint(int index, Point bendpoint) {
        getBendpoints().set(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    @Override
    public String toString() {
        if (getParent() == null || target == null) {
            return "not_completed";
        }
        return getParent().toString() + " -> (" + getName() + ") -> " + target.toString();
    }
}

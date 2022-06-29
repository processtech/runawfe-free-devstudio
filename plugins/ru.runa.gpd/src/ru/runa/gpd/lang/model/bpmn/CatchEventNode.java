package ru.runa.gpd.lang.model.bpmn;

import java.util.List;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.Timer;

public class CatchEventNode extends AbstractEventNode implements IReceiveMessageNode, IBoundaryEventCapable, IBoundaryEventContainer {

    public static boolean isBoundaryEventInParent(GraphElement parent) {
        return parent instanceof IBoundaryEventContainer && !(parent.getParent() instanceof IBoundaryEventContainer);
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    protected void validateOnEmptyRules(List<ValidationError> errors) {
        if (getEventNodeType() == EventNodeType.error && getParent() instanceof IBoundaryEventContainer) {
            return;
        }
        super.validateOnEmptyRules(errors);
    }

    @Override
    public void updateBoundaryEventConstraint() {
        getConstraint().setX(getParent().getConstraint().width - getConstraint().width);
        getConstraint().setY(getParent().getConstraint().height - getConstraint().height);
    }

    @Override
    public boolean isBoundaryEvent() {
        return isBoundaryEventInParent(getParent());
    }
}

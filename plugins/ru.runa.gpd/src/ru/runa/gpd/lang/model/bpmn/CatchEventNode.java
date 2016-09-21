package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.Timer;

public class CatchEventNode extends AbstractEventNode implements IReceiveMessageNode, IBoundaryEvent {

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

}

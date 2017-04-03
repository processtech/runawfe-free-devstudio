package ru.runa.gpd.editor.graphiti.add;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;

public class AddEventNodeFeature extends AddNodeWithImageFeature {

    @Override
    protected String getIcon(Node node) {
        AbstractEventNode eventNode = (AbstractEventNode) node;
        return eventNode.getEventNodeType().getImageName(eventNode instanceof CatchEventNode, false);
    }
}

package ru.runa.gpd.editor.gef.part.graph;

import ru.runa.gpd.lang.model.jpdl.SendMessageNode;

public class SendMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public SendMessageNode getModel() {
        return (SendMessageNode) super.getModel();
    }
}

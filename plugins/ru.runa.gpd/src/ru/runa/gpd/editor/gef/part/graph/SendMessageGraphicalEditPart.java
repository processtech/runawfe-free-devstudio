package ru.runa.gpd.editor.gef.part.graph;

import ru.runa.gpd.lang.model.jpdl.ThrowEventNode;

public class SendMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public ThrowEventNode getModel() {
        return (ThrowEventNode) super.getModel();
    }
}

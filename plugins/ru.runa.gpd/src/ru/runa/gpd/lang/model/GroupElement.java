package ru.runa.gpd.lang.model;

import org.eclipse.swt.graphics.Image;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class GroupElement extends GraphElement {
    private final NodeTypeDefinition typeDefinition;

    public GroupElement(ProcessDefinition definition, NodeTypeDefinition typeDefinition) {
        setParent(definition);
        this.typeDefinition = typeDefinition;
    }

    @Override
    public NodeTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    @Override
    public String getLabel() {
        return getTypeDefinition().getLabel();
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/group.gif");
    }

}

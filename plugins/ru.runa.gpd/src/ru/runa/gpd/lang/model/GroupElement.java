package ru.runa.gpd.lang.model;

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
}

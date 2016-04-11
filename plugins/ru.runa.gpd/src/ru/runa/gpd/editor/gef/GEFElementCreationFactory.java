package ru.runa.gpd.editor.gef;

import org.eclipse.gef.requests.CreationFactory;

import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GEFElementCreationFactory implements CreationFactory {
    private final NodeTypeDefinition nodeTypeDefinition;
    private final ProcessDefinition definition;

    public GEFElementCreationFactory(NodeTypeDefinition nodeTypeDefinition, ProcessDefinition definition) {
        this.nodeTypeDefinition = nodeTypeDefinition;
        this.definition = definition;
    }

    @Override
    public Object getNewObject() {
        return nodeTypeDefinition.createElement(definition, true);
    }

    public Object getNewObject(GraphElement parent) {
        return nodeTypeDefinition.createElement(parent, true);
    }

    @Override
    public Object getObjectType() {
        return nodeTypeDefinition.getJpdlElementName();
    }
}

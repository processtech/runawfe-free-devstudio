package ru.runa.gpd.editor.graphiti;

import org.eclipse.draw2d.geometry.Point;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

public class TextDecoratorEmulation {
    // TODO blurred logic, see this commit fully
    protected TextDecorationNode definition;
    protected Point definitionLocation = new Point();
    protected boolean hasDefinitionLocation = false;
    protected Node parent;

    public TextDecoratorEmulation(Node parent) {
        this.parent = parent;
    }

    public void setDefinitionLocation(Point point) {
        definitionLocation.setLocation(point);
        hasDefinitionLocation = true;
    }

    public Point getDefinitionLocation() {
        return definitionLocation;
    }

    public boolean hasDefinitionLocation() {
        return hasDefinitionLocation;
    }

    public void link(TextDecorationNode node) {
        definition = node;
        node.setTarget(parent);
    }

    public TextDecorationNode getDefinition() {
        return definition;
    }

}

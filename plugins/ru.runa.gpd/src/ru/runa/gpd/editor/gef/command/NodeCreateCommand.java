package ru.runa.gpd.editor.gef.command;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;

public class NodeCreateCommand extends Command {
    private Node node;
    private Rectangle constraint;
    protected ProcessDefinition definition;

    @Override
    public void execute() {
        node.setConstraint(constraint);
        definition.addChild(node);
    }

    @Override
    public boolean canExecute() {
        if (node instanceof StartState) {
            return definition.getFirstChild(StartState.class) == null;
        }
        return true;
    }

    @Override
    public void undo() {
        definition.removeChild(node);
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setConstraint(Rectangle constraint) {
        this.constraint = constraint;
    }

    public void setParent(ProcessDefinition parent) {
        this.definition = parent;
    }
}

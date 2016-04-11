package ru.runa.gpd.editor.gef.command;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import ru.runa.gpd.lang.model.Node;

public class NodeChangeConstraintCommand extends Command {
    private final ChangeBoundsRequest request;
    private final Rectangle newConstraint;
    private final Node node;
    private Rectangle oldConstraint;

    public NodeChangeConstraintCommand(ChangeBoundsRequest request, Node node, Rectangle newConstraint) {
        this.request = request;
        this.newConstraint = newConstraint;
        this.node = node;
    }

    @Override
    public void execute() {
        oldConstraint = node.getConstraint();
        node.setConstraint(newConstraint);
    }

    @Override
    public void undo() {
        node.setConstraint(oldConstraint);
    }
}

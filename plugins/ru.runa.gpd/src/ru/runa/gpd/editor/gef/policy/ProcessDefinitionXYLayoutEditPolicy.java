package ru.runa.gpd.editor.gef.policy;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.gef.command.NodeChangeConstraintCommand;
import ru.runa.gpd.editor.gef.command.NodeCreateCommand;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class ProcessDefinitionXYLayoutEditPolicy extends XYLayoutEditPolicy {
    @Override
    protected Command createAddCommand(EditPart child, Object constraint) {
        return null;
    }

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        IFigure figure = ((GraphicalEditPart) child).getFigure();
        if (figure instanceof NodeFigure && !((NodeFigure) figure).isResizeable()) {
            return new NonResizableEditPolicy();
        }
        return new ResizableEditPolicy();
    }

    @Override
    protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
        Rectangle newRect = getClosestRectangle((Rectangle) constraint);
        NodeChangeConstraintCommand locationCommand = new NodeChangeConstraintCommand(request, (Node) child.getModel(), newRect);
        return locationCommand;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        Object newObject = request.getNewObject();
        if (newObject instanceof Node) {
            NodeCreateCommand createCommand = new NodeCreateCommand();
            createCommand.setNode((Node) newObject);
            createCommand.setParent((ProcessDefinition) getHost().getModel());
            Rectangle newRect = getClosestRectangle((Rectangle) getConstraintFor(request));
            createCommand.setConstraint(newRect);
            return createCommand;
        }
        return null;
    }

    @Override
    protected Command getDeleteDependantCommand(Request request) {
        return null;
    }

    private Rectangle getClosestRectangle(Rectangle rect) {
        int xCount = (int) Math.round((double) rect.x / GEFConstants.GRID_SIZE);
        int yCount = (int) Math.round((double) rect.y / GEFConstants.GRID_SIZE);
        int wCount = (int) Math.round((double) rect.width / GEFConstants.GRID_SIZE);
        int hCount = (int) Math.round((double) rect.height / GEFConstants.GRID_SIZE);
        return new Rectangle(xCount * GEFConstants.GRID_SIZE, yCount * GEFConstants.GRID_SIZE, wCount * GEFConstants.GRID_SIZE, hCount * GEFConstants.GRID_SIZE);
    }
}

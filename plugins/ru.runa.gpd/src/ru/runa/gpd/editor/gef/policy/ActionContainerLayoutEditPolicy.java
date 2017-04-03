package ru.runa.gpd.editor.gef.policy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.DropRequest;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.gef.ActionGraphUtils;
import ru.runa.gpd.editor.gef.command.AddActionCommand;
import ru.runa.gpd.editor.gef.command.MoveActionCommand;
import ru.runa.gpd.editor.gef.part.graph.ActionGraphicalEditPart;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class ActionContainerLayoutEditPolicy extends OrderedLayoutEditPolicy {

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        return new ActionDecorationEditPolicy();
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        if (request.getNewObject() instanceof Action) {
            AddActionCommand command = new AddActionCommand();
            command.setTarget((ActionContainer) getHost().getModel());
            EditPart after = getInsertionReference(request);
            int newIndex = getHost().getChildren().indexOf(after);
            command.setActionIndex(newIndex);
            return command;
        }
        return null;
    }

    @Override
    protected Command createAddCommand(EditPart child, EditPart after) {
        ActionContainer newTarget = (ActionContainer) getHost().getModel();
        if (child instanceof ActionGraphicalEditPart) {
            ActionGraphicalEditPart actionEditPart = (ActionGraphicalEditPart) child;
            int newIndex = getHost().getChildren().indexOf(after);
            return new MoveActionCommand(newTarget, actionEditPart.getModel(), newIndex);
        }
        return null;
    }

    @Override
    protected Command createMoveChildCommand(EditPart child, EditPart after) {
        return createAddCommand(child, after);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected EditPart getInsertionReference(Request request) {
        List<EditPart> children = getHost().getChildren();

        if (request.getType().equals(RequestConstants.REQ_CREATE)) {
            int i = getFeedbackIndexFor(request);
            if (i == -1) {
                return null;
            }
            return children.get(i);
        }

        int index = getFeedbackIndexFor(request);
        if (index != -1) {
            List<EditPart> selection = getHost().getViewer().getSelectedEditParts();
            do {
                EditPart editpart = children.get(index);
                if (!selection.contains(editpart)) {
                    return editpart;
                }
            } while (++index < children.size());
        }
        return null; // Not found, add at the end.
    }

    @SuppressWarnings("unchecked")
    private int getFeedbackIndexFor(Request request) {
        Point mouseLocation = getLocationFromRequest(request);
        double minDistance = ActionGraphUtils.ACTION_SIZE;
        int candidate = -1;
        List<GraphicalEditPart> children = getHost().getChildren();
        for (int i = 0; i < children.size(); i++) {
            double distance = getAbsoluteBounds(children.get(i)).getCenter().getDistance(mouseLocation);
            if (distance < minDistance) {
                minDistance = distance;
                candidate = i;
            }
        }
        return candidate;
    }

    @Override
    protected void eraseLayoutTargetFeedback(Request request) {
        if (feedbackFigure != null) {
            removeFeedback(feedbackFigure);
            feedbackFigure = null;
        }
    }

    private IFigure feedbackFigure = null;

    private IFigure getFeedbackFigure() {
        if (feedbackFigure == null) {
            feedbackFigure = new ImageFigure(SharedImages.getImage("icons/insertion_cursor.gif"));
            feedbackFigure.setSize(ActionGraphUtils.ACTION_SIZE, ActionGraphUtils.ACTION_SIZE);
        }
        return feedbackFigure;
    }

    private Point getLocationFromRequest(Request request) {
        return ((DropRequest) request).getLocation();
    }

    @Override
    protected void showLayoutTargetFeedback(Request request) {
        if (getHost().getChildren().size() == 0) {
            return;
        }
        if (!(request instanceof CreateRequest)) {
            return;
        }
        if (!(((CreateRequest) request).getNewObject() instanceof Action)) {
            return;
        }

        int epIndex = getFeedbackIndexFor(request);
        if (epIndex == -1) {
            epIndex = getHost().getChildren().size();
        }
        Point feedbackPoint = ActionGraphUtils.getActionFigureLocation(((GraphicalEditPart) getHost()).getFigure(), epIndex, getHost().getChildren()
                .size(), true);
        getFeedbackFigure().setLocation(feedbackPoint);
        addFeedback(getFeedbackFigure());
    }

    private Rectangle getAbsoluteBounds(GraphicalEditPart ep) {
        Rectangle bounds = ep.getFigure().getBounds().getCopy();
        ep.getFigure().translateToAbsolute(bounds);
        return bounds;
    }

    private static class ActionDecorationEditPolicy extends NonResizableEditPolicy {

        @Override
        protected List<Handle> createSelectionHandles() {
            GraphicalEditPart editPart = (GraphicalEditPart) getHost();
            List<Handle> list = new ArrayList<Handle>();
            // use custom drag trackers
            NonResizableHandleKit.addMoveHandle(editPart, list, editPart.getDragTracker(null), Cursors.SIZEALL);
            NonResizableHandleKit.addCornerHandles(editPart, list, editPart.getDragTracker(null), Cursors.SIZEALL);
            return list;
        }
    }
}

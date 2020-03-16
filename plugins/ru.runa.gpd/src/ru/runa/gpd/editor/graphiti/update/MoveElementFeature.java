package ru.runa.gpd.editor.graphiti.update;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

public class MoveElementFeature extends DefaultMoveShapeFeature implements CustomUndoRedoFeature {

    private Rectangle undoConstraint;
    private Rectangle redoConstraint;
    private List<List<org.eclipse.draw2d.geometry.Point>> undoBendpointsList;
    private List<List<org.eclipse.draw2d.geometry.Point>> redoBendpointsList;
    private int dX;
    private int dY;

    public MoveElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (element instanceof Action) {
            return false;
        }
        if (element instanceof Timer && element.getParent() instanceof ITimed) {
            return false;
        }
        if (element instanceof CatchEventNode && element.getParent() instanceof IBoundaryEventContainer) {
            return false;
        }
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (element instanceof Swimlane) {
            return parentObject instanceof ProcessDefinition;
        }
        return parentObject instanceof Swimlane || parentObject instanceof ProcessDefinition;
    }

    @Override
    protected void postMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        undoConstraint = element.getConstraint().getCopy();
        dX = context.getDeltaX();
        dY = context.getDeltaY();
        Rectangle newConstraint = element.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        element.setConstraint(newConstraint);
        if (element instanceof Node) {
            // move transition bendpoints
            List<Anchor> anchors = getAnchors(shape);
            List<FreeFormConnection> connections = Lists.newArrayList();
            for (Anchor anchor : anchors) {
                for (Connection connection : anchor.getOutgoingConnections()) {
                    if (connection instanceof FreeFormConnection) {
                        connections.add((FreeFormConnection) connection);
                    }
                }
            }
            undoBendpointsList = new ArrayList<List<org.eclipse.draw2d.geometry.Point>>();
            redoBendpointsList = new ArrayList<List<org.eclipse.draw2d.geometry.Point>>();
            for (FreeFormConnection connection : connections) {
                Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
                List<Point> points = connection.getBendpoints();
                if (points.size() != transition.getBendpoints().size()) {
                    throw new RuntimeException("connection.getBendpoints().size() != transition.getBendpoints().size() for " + transition);
                }
                List<org.eclipse.draw2d.geometry.Point> undoBendpoints = new ArrayList<>();
                List<org.eclipse.draw2d.geometry.Point> redoBendpoints = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    Point diagramPoint = points.get(i);
                    org.eclipse.draw2d.geometry.Point modelPoint = transition.getBendpoints().get(i);
                    undoBendpoints.add(new org.eclipse.draw2d.geometry.Point(new org.eclipse.draw2d.geometry.Point(modelPoint)));
                    redoBendpoints.add(new org.eclipse.draw2d.geometry.Point(diagramPoint.getX(), diagramPoint.getY()));
                    if (modelPoint.x != diagramPoint.getX() || modelPoint.y != diagramPoint.getY()) {
                        transition.setBendpoint(i, new org.eclipse.draw2d.geometry.Point(diagramPoint.getX(), diagramPoint.getY()));
                    }
                }
                undoBendpointsList.add(undoBendpoints);
                redoBendpointsList.add(redoBendpoints);
            }
        }
        reconnectToParent(context, element);

        // move definition with point
        if (element instanceof HasTextDecorator) {
            HasTextDecorator withDefinition = (HasTextDecorator) element;
            Rectangle defPosition = withDefinition.getTextDecoratorEmulation().getDefinition().getConstraint().getCopy();
            defPosition.setX(defPosition.x + context.getDeltaX());
            defPosition.setY(defPosition.y + context.getDeltaY());
            withDefinition.getTextDecoratorEmulation().getDefinition().setConstraint(defPosition);
            Graphiti.getGaService().setLocation(
                    withDefinition.getTextDecoratorEmulation().getDefinition().getUiContainer().getOwner().getGraphicsAlgorithm(), defPosition.x,
                    defPosition.y);

            // re-attach TextDecorator parent on graphical layer
            // https://sourceforge.net/p/runawfe/bugs/685/
            TextDecoratorEmulation textDecoratorEmulation = withDefinition.getTextDecoratorEmulation();
            if (context.getSourceContainer() != context.getTargetContainer()) {
                Shape textShape = (Shape) textDecoratorEmulation.getDefinition().getUiContainer().getOwner();
                textShape.setContainer(shape.getContainer());
            }
            textDecoratorEmulation.setDefinitionLocation(defPosition.getLocation());
        }
        // if text decoration moved
        ifTextDecorationMoved(element, newConstraint);
    }

    private List<Anchor> getAnchors(Shape theShape) {
        List<Anchor> ret = new ArrayList<Anchor>();
        ret.addAll(theShape.getAnchors());
        if (theShape instanceof ContainerShape) {
            ContainerShape containerShape = (ContainerShape) theShape;
            List<Shape> children = containerShape.getChildren();
            for (Shape shape : children) {
                if (shape instanceof ContainerShape) {
                    ret.addAll(getAnchors(shape));
                } else {
                    ret.addAll(shape.getAnchors());
                }
            }
        }
        return ret;
    }

    @Override
    public boolean canUndo(IContext context) {
        return undoConstraint != null;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IMoveShapeContext) {
            IMoveShapeContext workContext = (IMoveShapeContext) context;
            Shape shape = workContext.getShape();
            GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
            redoConstraint = element.getConstraint().getCopy();
            element.setConstraint(undoConstraint);

            // move transition bendpoints
            moveTransitionBendpointsUndoRedo(shape, element, undoBendpointsList);

            reconnectToParent(workContext, element);

            // move definition with point
            moveDefinitionWithPoint(element, -dX, -dY);

            // if text decoration moved
            ifTextDecorationMoved(element, undoConstraint);
        }
    }

    private void moveTransitionBendpointsUndoRedo(Shape shape, GraphElement element, List<List<org.eclipse.draw2d.geometry.Point>> bendpointList) {
        if (element instanceof Node) {
            List<Anchor> anchors = getAnchors(shape);
            List<FreeFormConnection> connections = Lists.newArrayList();
            for (Anchor anchor : anchors) {
                for (Connection connection : anchor.getOutgoingConnections()) {
                    if (connection instanceof FreeFormConnection) {
                        connections.add((FreeFormConnection) connection);
                    }
                }
            }
            for (int c = 0; c < connections.size(); c++) {
                Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connections.get(c));
                List<Point> points = connections.get(c).getBendpoints();
                for (int i = 0; i < points.size(); i++) {
                    Point diagramPoint = points.get(i);
                    org.eclipse.draw2d.geometry.Point modelPoint = transition.getBendpoints().get(i);
                    if (modelPoint.x != diagramPoint.getX() || modelPoint.y != diagramPoint.getY()) {
                        transition.setBendpoint(i, new org.eclipse.draw2d.geometry.Point(bendpointList.get(c).get(i)));
                    }
                }
            }
        }
    }

    private void reconnectToParent(IMoveShapeContext workContext, GraphElement element) {
        if (workContext.getSourceContainer() != workContext.getTargetContainer()) {
            GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(workContext.getTargetContainer());
            element.setParentContainer(parent);
            if (element instanceof SwimlanedNode) {
                Swimlane swimlane = null;
                if (parent instanceof Swimlane) {
                    swimlane = (Swimlane) parent;
                }
                ((SwimlanedNode) element).setSwimlane(swimlane);
            }
        }
    }

    private void moveDefinitionWithPoint(GraphElement element, int xDelta, int yDelta) {
        if (element instanceof HasTextDecorator) {
            HasTextDecorator withDefinition = (HasTextDecorator) element;
            Rectangle defPosition = withDefinition.getTextDecoratorEmulation().getDefinition().getConstraint().getCopy();
            defPosition.setX(defPosition.x + xDelta);
            defPosition.setY(defPosition.y + yDelta);
            withDefinition.getTextDecoratorEmulation().getDefinition().setConstraint(defPosition);
            TextDecoratorEmulation textDecoratorEmulation = withDefinition.getTextDecoratorEmulation();
            textDecoratorEmulation.setDefinitionLocation(defPosition.getLocation());
        }
    }

    private void ifTextDecorationMoved(GraphElement element, Rectangle constraint) {
        if (element instanceof TextDecorationNode) {
            TextDecorationNode graph = (TextDecorationNode) element;
            HasTextDecorator withText = (HasTextDecorator) graph.getTarget();
            withText.getTextDecoratorEmulation().setDefinitionLocation(constraint.getLocation());
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return redoConstraint != null;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IMoveShapeContext) {
            IMoveShapeContext workContext = (IMoveShapeContext) context;
            Shape shape = workContext.getShape();
            GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
            element.setConstraint(redoConstraint);

            // move transition bendpoints
            moveTransitionBendpointsUndoRedo(shape, element, redoBendpointsList);

            reconnectToParent(workContext, element);

            // move definition with point
            moveDefinitionWithPoint(element, dX, dY);

            // if text decoration moved
            ifTextDecorationMoved(element, redoConstraint);
        }

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}

package ru.runa.gpd.editor.graphiti.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TextDecorationNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

import com.google.common.collect.Lists;

public class MoveElementFeature extends DefaultMoveShapeFeature {
    public MoveElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (element instanceof Timer && element.getParent() instanceof ITimed) {
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
        Rectangle newConstraint = element.getConstraint().getCopy();
        newConstraint.x = context.getX();
        newConstraint.y = context.getY();
        element.setConstraint(newConstraint);
        if (element instanceof Node) {
            // move transition bendpoints
            List<Anchor> anchros = getAnchors(shape);
            List<FreeFormConnection> connections = Lists.newArrayList();
            for (Anchor anchor : anchros) {
                for (Connection connection : anchor.getOutgoingConnections()) {
                    if (connection instanceof FreeFormConnection) {
                        connections.add((FreeFormConnection) connection);
                    }
                }
            }
            for (FreeFormConnection connection : connections) {
                Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
                List<Point> points = connection.getBendpoints();
                if (points.size() != transition.getBendpoints().size()) {
                    throw new RuntimeException("connection.getBendpoints().size() != transition.getBendpoints().size() for " + transition);
                }
                for (int i = 0; i < points.size(); i++) {
                    Point diagramPoint = points.get(i);
                    org.eclipse.draw2d.geometry.Point modelPoint = transition.getBendpoints().get(i);
                    if (modelPoint.x != diagramPoint.getX() || modelPoint.y != diagramPoint.getY()) {
                        transition.setBendpoint(i, new org.eclipse.draw2d.geometry.Point(diagramPoint.getX(), diagramPoint.getY()));
                    }
                }
            }
        }
        if (context.getSourceContainer() != context.getTargetContainer()) {
            GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
            element.setParentContainer(parent);
            if (element instanceof SwimlanedNode) {
                Swimlane swimlane = null;
                if (parent instanceof Swimlane) {
                    swimlane = (Swimlane) parent;
                }
                ((SwimlanedNode) element).setSwimlane(swimlane);
            }
        }
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
        if (element instanceof TextDecorationNode) {
            TextDecorationNode graph = (TextDecorationNode) element;
            HasTextDecorator withText = (HasTextDecorator) graph.getTarget();
            withText.getTextDecoratorEmulation().setDefinitionLocation(newConstraint.getLocation());
        }
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
}

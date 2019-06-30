package ru.runa.gpd.editor.graphiti.create;

import java.util.List;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

public class CreateElementFeature extends AbstractCreateFeature implements GEFConstants, ICustomUndoRedoFeature {
    public static final String CONNECTION_PROPERTY = "connectionContext";
    private NodeTypeDefinition nodeDefinition;
    private DiagramFeatureProvider featureProvider;
    private GraphElement graphElement;
    private List<Transition> leavingTransitions;
    private List<Transition> arrivingTransitions;

    public CreateElementFeature() {
        super(null, "", "");
    }

    public void setNodeDefinition(NodeTypeDefinition nodeDefinition) {
        this.nodeDefinition = nodeDefinition;
    }

    public void setFeatureProvider(DiagramFeatureProvider provider) {
        this.featureProvider = provider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public String getCreateName() {
        return nodeDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return getNodeDefinition().getPaletteIcon();
    }

    public NodeTypeDefinition getNodeDefinition() {
        return nodeDefinition;
    }

    protected ProcessDefinition getProcessDefinition() {
        return getFeatureProvider().getCurrentProcessDefinition();
    }

    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return (parentObject instanceof ProcessDefinition || parentObject instanceof Swimlane);
    }

    @Override
    public Object[] create(ICreateContext context) {
        graphElement = getNodeDefinition().createElement(getProcessDefinition(), true);
        GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (graphElement instanceof Action) {
            if (context.getTargetConnection() != null) {
                parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetConnection());
            }
            ((Action) graphElement).setName(getCreateName() + " " + (parent.getChildren(Action.class).size() + 1));
        }
        graphElement.setParentContainer(parent);
        Swimlane swimlane = null;
        if (parent instanceof Swimlane) {
            swimlane = (Swimlane) parent;
            parent = parent.getParent();
        }
        if (graphElement instanceof SwimlanedNode) {
            ((SwimlanedNode) graphElement).setSwimlane(swimlane);
        }
        if (!(parent instanceof IBoundaryEventContainer) || parent.getChildren(graphElement.getClass()).size() < 1) {
            parent.addChild(graphElement);
            CreateConnectionContext connectionContext = (CreateConnectionContext) context.getProperty(CONNECTION_PROPERTY);
            setLocationAndSize(graphElement, (CreateContext) context, connectionContext);
            PictogramElement element = addGraphicalRepresentation(context, graphElement);
            if (connectionContext != null && graphElement instanceof Node) {
                connectionContext.setTargetPictogramElement(element);
                connectionContext.setTargetAnchor(Graphiti.getPeService().getChopboxAnchor((AnchorContainer) element));
                CreateTransitionFeature createTransitionFeature = new CreateTransitionFeature();
                createTransitionFeature.setFeatureProvider(featureProvider);
                createTransitionFeature.create(connectionContext);
            }
            return new Object[] { graphElement };
        }
        return null;
    }

    private void setLocationAndSize(GraphElement element, CreateContext context, CreateConnectionContext connectionContext) {
        Dimension defaultSize = element.getTypeDefinition().getGraphitiEntry().getDefaultSize();
        if (connectionContext != null) {
            PictogramElement sourceElement = connectionContext.getSourcePictogramElement();
            int xRight = sourceElement.getGraphicsAlgorithm().getX() + sourceElement.getGraphicsAlgorithm().getWidth();
            int yDelta = (defaultSize.height - sourceElement.getGraphicsAlgorithm().getHeight()) / 2;
            int shift = 5 * GRID_SIZE;
            GraphicsAlgorithm container = context.getTargetContainer().getGraphicsAlgorithm();
            if (container.getWidth() < xRight + shift + defaultSize.width) {
                shift = 2 * GRID_SIZE;
            }
            context.setLocation(xRight + shift, sourceElement.getGraphicsAlgorithm().getY() - yDelta);
        }
        if (context.getHeight() < defaultSize.height) {
            context.setHeight(defaultSize.height);
        }
        if (context.getWidth() < defaultSize.width) {
            context.setWidth(defaultSize.width);
        }
        element.setConstraint(new Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight()));
    }

    @Override
    public boolean canUndo(IContext context) {
        return graphElement != null;
    }

    @Override
    public void preUndo(IContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postUndo(IContext context) {
        if (graphElement instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) graphElement;
            textDecoration.getTarget().getParent().removeChild(textDecoration.getTarget());
            removeAndStoreTransitions(textDecoration.getTarget());
        } else if (graphElement instanceof HasTextDecorator) {
            HasTextDecorator withDefinition = (HasTextDecorator) graphElement;
            IDeleteContext delContext = new DeleteContext(withDefinition.getTextDecoratorEmulation().getDefinition().getUiContainer().getOwner());
            PictogramElement pe = delContext.getPictogramElement();
            Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);
            if (businessObjectsForPictogramElement != null) {
                for (Object buzinessObject : businessObjectsForPictogramElement) {
                    EcoreUtil.delete((EObject) buzinessObject, true);
                }
            }
        } else if (graphElement instanceof Node) {
            Node node = (Node) graphElement;
            removeAndStoreTransitions(node);
        }
        graphElement.getParent().removeChild(graphElement);
    }

    private void removeAndStoreTransitions(Node node) {
        leavingTransitions = node.getLeavingTransitions();
        for (Transition transition : leavingTransitions) {
            transition.getSource().removeLeavingTransition(transition);
        }
        arrivingTransitions = node.getArrivingTransitions();
        for (Transition transition : arrivingTransitions) {
            transition.getSource().removeLeavingTransition(transition);
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return graphElement != null;
    }

    @Override
    public void preRedo(IContext context) {
        // TODO Auto-generated method stub
    }

    @Override
    public void postRedo(IContext context) {
        if (graphElement instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) graphElement;
            textDecoration.getTarget().getParent().addChild(textDecoration.getTarget());
            restoreTransitions();
        } else {
            graphElement.getParent().addChild(graphElement);
        }
        if (graphElement instanceof Node) {
            restoreTransitions();
        }
    }

    private void restoreTransitions() {
        if (leavingTransitions != null) {
            for (Transition transition : leavingTransitions) {
                transition.getSource().addChild(transition);
            }
        }
        if (arrivingTransitions != null) {
            for (Transition transition : arrivingTransitions) {
                transition.getSource().addChild(transition);
            }
        }
    }
}

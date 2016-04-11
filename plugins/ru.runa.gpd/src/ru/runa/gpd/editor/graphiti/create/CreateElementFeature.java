package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class CreateElementFeature extends AbstractCreateFeature implements GEFConstants {
    public static final String CONNECTION_PROPERTY = "connectionContext";
    private NodeTypeDefinition nodeDefinition;
    private DiagramFeatureProvider featureProvider;

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
        GraphElement graphElement = getNodeDefinition().createElement(getProcessDefinition(), true);
        GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        graphElement.setParentContainer(parent);
        Swimlane swimlane = null;
        if (parent instanceof Swimlane) {
            swimlane = (Swimlane) parent;
            parent = parent.getParent();
        }
        if (graphElement instanceof SwimlanedNode) {
            ((SwimlanedNode) graphElement).setSwimlane(swimlane);
        }
        parent.addChild(graphElement);
        CreateConnectionContext connectionContext = (CreateConnectionContext) context.getProperty(CONNECTION_PROPERTY);
        setLocation(graphElement, (CreateContext) context, connectionContext);
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

    private void setLocation(GraphElement target, CreateContext context, CreateConnectionContext connectionContext) {
        if (connectionContext != null) {
            PictogramElement sourceElement = connectionContext.getSourcePictogramElement();
            int xRight = sourceElement.getGraphicsAlgorithm().getX() + sourceElement.getGraphicsAlgorithm().getWidth();
            Dimension targetSize = target.getTypeDefinition().getGraphitiEntry().getDefaultSize();
            int yDelta = (targetSize.height - sourceElement.getGraphicsAlgorithm().getHeight()) / 2;
            int shift = 5 * GRID_SIZE;
            GraphicsAlgorithm container = context.getTargetContainer().getGraphicsAlgorithm();
            if (container.getWidth() < xRight + shift + targetSize.width) {
                shift = 2 * GRID_SIZE;
            }
            context.setLocation(xRight + shift, sourceElement.getGraphicsAlgorithm().getY() - yDelta);
        }
    }
}

package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;

import ru.runa.gpd.editor.graphiti.GraphUtil;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class CreateTransitionFeature extends AbstractCreateConnectionFeature {
    private final NodeTypeDefinition transitionDefinition;
    private IFeatureProvider featureProvider;

    public CreateTransitionFeature() {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
    }

    public void setFeatureProvider(IFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public String getCreateName() {
        return transitionDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return transitionDefinition.getPaletteIcon();
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        if (source instanceof Node) {
            Node sourceNode = (Node) source;
            return sourceNode.canAddLeavingTransition();
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        Object target = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (target instanceof Node) {
            return ((Node) target).canAddArrivingTransition((Node) source);
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Node source = (Node) getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        Node target = (Node) getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        // create new business object
        Transition transition = transitionDefinition.createElement(source, false);
        transition.setTarget(target);
        transition.setName(source.getNextTransitionName(transitionDefinition));
        source.addLeavingTransition(transition);
        // add connection for business object
        Anchor sourceAnchor = context.getSourceAnchor();
        if (sourceAnchor == null) {
            sourceAnchor = GraphUtil.getChopboxAnchor(context.getSourcePictogramElement());
        }
        Anchor targetAnchor = context.getTargetAnchor();
        if (targetAnchor == null) {
            targetAnchor = GraphUtil.getChopboxAnchor(context.getTargetPictogramElement());
        }
        AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
        addConnectionContext.setNewObject(transition);
        return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
    }
}

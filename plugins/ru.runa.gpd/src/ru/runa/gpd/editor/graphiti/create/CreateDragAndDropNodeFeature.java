package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

// bizagi behaviour, unused now
public class CreateDragAndDropNodeFeature extends AbstractCreateConnectionFeature {
    private final NodeTypeDefinition transitionDefinition;
    private IFeatureProvider featureProvider;
    private final ProcessDefinition processDefinition;

    public CreateDragAndDropNodeFeature(ProcessDefinition processDefinition) {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        this.processDefinition = processDefinition;
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
        Node source = getNode(context.getSourceAnchor());
        if (source != null && !(source instanceof EndState)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        return true;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Node source = getNode(context.getSourceAnchor());
        Node target = createTarget(context);
        // create new business object
        Transition transition = transitionDefinition.createElement(source, false);
        transition.setTarget(target);
        transition.setName(source.getNextTransitionName(transitionDefinition));
        source.addLeavingTransition(transition);
        // add connection for business object
        AddConnectionContext addConnectionContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
        addConnectionContext.setNewObject(transition);
        return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
    }

    private Node createTarget(ICreateConnectionContext context) {
        CreateContext createContext = new CreateContext();
        createContext.setLocation(context.getTargetLocation().getX(), context.getTargetLocation().getY());
        createContext.setSize(30, 30);
        //createContext.setTargetConnection(targetConnection);
        createContext.setTargetContainer(getDiagram());
        Node node = NodeRegistry.getNodeTypeDefinition(Decision.class).createElement(processDefinition, true);
        Object parent = getBusinessObjectForPictogramElement(createContext.getTargetContainer());
        ((Node) parent).addChild(node);
        PictogramElement element = getFeatureProvider().addIfPossible(new AddContext(createContext, node));
        ((CreateConnectionContext) context).setTargetPictogramElement(element);
        ((CreateConnectionContext) context).setTargetAnchor(Graphiti.getPeService().getChopboxAnchor((AnchorContainer) element));
        //            CreateTransitionFeature createTransitionFeature = new CreateTransitionFeature();
        //            createTransitionFeature.setFeatureProvider(featureProvider);
        //            createTransitionFeature.create(connectionContext);
        return node;
    }

    private Node getNode(Anchor anchor) {
        if (anchor != null) {
            return (Node) getBusinessObjectForPictogramElement(anchor.getParent());
        }
        return null;
    }
}

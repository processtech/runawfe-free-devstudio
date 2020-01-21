package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DataStore;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class CreateDottedTransitionFeature extends AbstractCreateConnectionFeature {
    private final NodeTypeDefinition transitionDefinition;
    private IFeatureProvider featureProvider;

    public CreateDottedTransitionFeature() {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(DottedTransition.class);
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        final Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        final Object target = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if ((source instanceof ScriptTask && target instanceof DataStore) || (source instanceof DataStore && target instanceof ScriptTask)) {
            return ((ConnectableViaDottedTransition) target).canAddArrivingDottedTransition((ConnectableViaDottedTransition) source);
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        final Node source = (Node) getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        final Node target = (Node) getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        // create new business object
        final DottedTransition transition = transitionDefinition.createElement(source, false);
        transition.setTarget(target);

        if (source instanceof DataStore) {
            final ScriptTask scriptTask = (ScriptTask) target;
            scriptTask.setUseExternalStorageIn(true);
        } else {
            final ScriptTask scriptTask = (ScriptTask) source;
            scriptTask.setUseExternalStorageOut(true);
        }

        transition.setName(source.getNextTransitionName(transitionDefinition));
        ((ConnectableViaDottedTransition) source).addLeavingDottedTransition(transition);
        // add connection for business object
        Anchor sourceAnchor = context.getSourceAnchor();
        if (sourceAnchor == null) {
            sourceAnchor = getChopboxAnchor(context.getSourcePictogramElement());
        }
        Anchor targetAnchor = context.getTargetAnchor();
        if (targetAnchor == null) {
            targetAnchor = getChopboxAnchor(context.getTargetPictogramElement());
        }
        final AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
        addConnectionContext.setNewObject(transition);
        return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        final Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        if (source instanceof ConnectableViaDottedTransition) {
            return ((ConnectableViaDottedTransition) source).canAddLeavingDottedTransition();
        }
        return false;
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

    private Anchor getChopboxAnchor(PictogramElement pe) {
        if (pe instanceof AnchorContainer) {
            Anchor anchor = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
            if (anchor != null) {
                return anchor;
            }
        }
        if (pe instanceof ContainerShape) {
            for (Shape shape : ((ContainerShape) pe).getChildren()) {
                Anchor anchor = getChopboxAnchor(shape);
                if (anchor != null) {
                    return anchor;
                }
            }
        }
        return null;
    }

}

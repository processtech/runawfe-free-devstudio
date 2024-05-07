package ru.runa.gpd.editor.graphiti.create;

import java.util.Comparator;
import java.util.List;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.AbstractTransition;

public abstract class CreateAbstractTransitionFeature extends AbstractCreateConnectionFeature implements CustomUndoRedoFeature {
    protected NodeTypeDefinition transitionDefinition;
    protected IFeatureProvider featureProvider;

    protected CreateAbstractTransitionFeature(Class<? extends AbstractTransition> clazz) {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(clazz);
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    public void setFeatureProvider(IFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
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
    public boolean canUndo(IContext context) {
        return getTransition(context) != null;
    }

    @Override
    public boolean canRedo(IContext context) {
        return context.getProperty(CreateElementFeature.CONNECTION_PROPERTY) != null;
    }

    protected abstract List<? extends AbstractTransition> getLeavingTransitions(Object source);

    protected AbstractTransition getTransition(IContext context) {
        Object sourceElement = getBusinessObjectForPictogramElement(((ICreateConnectionContext) context).getSourcePictogramElement());
        List<? extends AbstractTransition> leavingTransitions = getLeavingTransitions(sourceElement);
        return leavingTransitions.stream().max(Comparator.comparing(AbstractTransition::getIdNumber)).orElse(null);
    }

    protected Anchor getChopboxAnchor(PictogramElement pe) {
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

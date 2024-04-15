package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;

public class UpdateEventNodeFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        if (context.getPictogramElement() instanceof ContainerShape) {
            ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
            AbstractEventNode eventNode = (AbstractEventNode) getBusinessObjectForPictogramElement(containerShape);
            if (!Objects.equal(((Image) containerShape.getGraphicsAlgorithm()).getId(), getImageId(eventNode))) {
                return Reason.createTrueReason();
            }
            String tooltip = PropertyUtil.getPropertyValue(containerShape, GaProperty.TOOLTIP);
            if (!Objects.equal(tooltip, eventNode.getTooltip())) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        AbstractEventNode eventNode = (AbstractEventNode) getBusinessObjectForPictogramElement(containerShape);
        String imageId = getImageId(eventNode);
        if (!Objects.equal(((Image) containerShape.getGraphicsAlgorithm()).getId(), imageId)) {
            Image oldImage = (Image) containerShape.getGraphicsAlgorithm();
            Image newImage = Graphiti.getGaService().createImage(containerShape, imageId);
            Graphiti.getGaService().setLocationAndSize(newImage, oldImage.getX(), oldImage.getY(), oldImage.getWidth(), oldImage.getHeight());
        }
        PropertyUtil.setPropertyValue(containerShape, GaProperty.TOOLTIP, eventNode.getTooltip());
        return true;
    }

    private String getImageId(AbstractEventNode eventNode) {
        boolean boundary = eventNode.getParent() instanceof IBoundaryEventContainer;
        return "graph/" + eventNode.getEventNodeType().getImageName(eventNode instanceof CatchEventNode, boundary);
    }
}

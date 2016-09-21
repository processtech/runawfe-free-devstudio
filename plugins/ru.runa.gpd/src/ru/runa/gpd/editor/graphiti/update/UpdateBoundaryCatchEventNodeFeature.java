package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;

public class UpdateBoundaryCatchEventNodeFeature extends UpdateEventNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        IReason reason = super.updateNeeded(context);
        if (context.getPictogramElement() instanceof ContainerShape) {
            ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
            CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(containerShape);
            if (catchEventNode.getParent() instanceof IBoundaryEventContainer
                    && catchEventNode.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
                return Reason.createTrueReason();
            }
        }
        return reason;
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(containerShape);
        if (catchEventNode.getParent() instanceof IBoundaryEventContainer
                && catchEventNode.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
            containerShape.getChildren().get(0).setVisible(!catchEventNode.isInterruptingBoundaryEvent());
            layoutPictogramElement(containerShape.getChildren().get(0));
        }
        return super.update(context);
    }

}

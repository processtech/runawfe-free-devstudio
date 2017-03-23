package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.editor.graphiti.GraphUtil;
import ru.runa.gpd.lang.model.IBoundaryEventContainer;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;

public class UpdateCatchEventNodeFeature extends UpdateEventNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (catchEventNode.getParent() instanceof IBoundaryEventContainer) {
            ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
            if (catchEventNode.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
                return Reason.createTrueReason();
            }
        }
        return super.updateNeeded(context);
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(containerShape);
        if (catchEventNode.getParent() instanceof IBoundaryEventContainer
                && catchEventNode.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
            GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
            GraphUtil.createBoundaryEventEllipse(getDiagram(), containerShape.getChildren().get(0), catchEventNode, ga.getWidth(), ga.getHeight());
            // this does not updates view immediately, only on diagram saving
            // containerShape.getChildren().get(0).setVisible(!catchEventNode.isInterruptingBoundaryEvent());
        }
        return super.update(context);
    }

}

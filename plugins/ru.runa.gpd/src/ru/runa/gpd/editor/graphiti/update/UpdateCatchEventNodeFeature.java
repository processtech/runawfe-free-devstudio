package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;

public class UpdateCatchEventNodeFeature extends UpdateEventNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (catchEventNode.isBoundaryEvent()) {
            GraphicsAlgorithm boundaryEllipse = PropertyUtil.findGaRecursiveByName(context.getPictogramElement(), GaProperty.BOUNDARY_ELLIPSE);
            if (catchEventNode.isInterruptingBoundaryEvent() == boundaryEllipse.getLineVisible()) {
                return Reason.createTrueReason();
            }
        }
        return super.updateNeeded(context);
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        CatchEventNode catchEventNode = (CatchEventNode) getBusinessObjectForPictogramElement(containerShape);
        if (catchEventNode.isBoundaryEvent()) {
            GraphicsAlgorithm boundaryEllipse = PropertyUtil.findGaRecursiveByName(containerShape, GaProperty.BOUNDARY_ELLIPSE);
            boundaryEllipse.setLineVisible(!catchEventNode.isInterruptingBoundaryEvent());
        }
        return super.update(context);
    }

}

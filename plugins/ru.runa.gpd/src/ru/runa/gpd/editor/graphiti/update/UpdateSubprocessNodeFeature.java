package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.editor.graphiti.layout.LayoutSubprocessNodeFeature;
import ru.runa.gpd.lang.model.Subprocess;

import com.google.common.base.Objects;

public class UpdateSubprocessNodeFeature extends UpdateStateNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Subprocess bo = (Subprocess) getBusinessObjectForPictogramElement(pe);
        String transaction = PropertyUtil.getPropertyValue(pe, GaProperty.TRANSACTION);
        if (transaction != null && !Objects.equal(transaction, String.valueOf(bo.isTransaction()))) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
    	ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
    	PictogramElement pe = context.getPictogramElement();
        Subprocess bo = (Subprocess) getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
        
        GraphicsAlgorithm secondBorder = PropertyUtil.findGaRecursiveByName(ga, LayoutSubprocessNodeFeature.SECOND_BORDER_RECT);
        if (bo.isTransaction()) {
        	secondBorder.setLineVisible(true);
        } else {
        	secondBorder.setLineVisible(false);
        }

        PropertyUtil.setPropertyValue(pe, GaProperty.TRANSACTION, String.valueOf(bo.isTransaction()));
        return super.update(context);
    }

}

package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
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

public class UpdateSubprocessNodeFeature extends UpdateStateNodeFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Subprocess bo = (Subprocess) getBusinessObjectForPictogramElement(pe);
        String transactional = PropertyUtil.getPropertyValue(pe, GaProperty.TRANSACTIONAL);
        String embedded = PropertyUtil.getPropertyValue(pe, GaProperty.EMBEDDED);

        if ((transactional != null && !Objects.equal(transactional, String.valueOf(bo.isTransactional())))
                || (embedded != null && !Objects.equal(embedded, String.valueOf(bo.isEmbedded())))) {
            return Reason.createTrueReason();
        }
        return super.updateNeeded(context);
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        PictogramElement pe = context.getPictogramElement();
        Subprocess bo = (Subprocess) getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();

        GraphicsAlgorithm secondBorder = PropertyUtil.findGaRecursiveByName(ga, LayoutSubprocessNodeFeature.SECOND_BORDER_RECT);
        if (bo.isTransactional()) {
            secondBorder.setLineVisible(true);
        } else {
            secondBorder.setLineVisible(false);
        }
        PropertyUtil.setPropertyValue(pe, GaProperty.TRANSACTIONAL, String.valueOf(bo.isTransactional()));

        GraphicsAlgorithm borderRect = PropertyUtil.findGaRecursiveByName(ga, LayoutSubprocessNodeFeature.BORDER_RECT);
        if (bo.isEmbedded()) {
            borderRect.setLineWidth(secondBorder.getStyle().getLineWidth());
        } else {
            borderRect.setLineWidth(secondBorder.getStyle().getLineWidth() + 1);
        }
        PropertyUtil.setPropertyValue(pe, GaProperty.EMBEDDED, String.valueOf(bo.isEmbedded()));

        return super.update(context);
    }

}

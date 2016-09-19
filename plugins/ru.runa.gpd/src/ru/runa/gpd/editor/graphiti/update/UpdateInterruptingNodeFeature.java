package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.editor.graphiti.IconUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.InterruptingNode;

import com.google.common.base.Objects;

public class UpdateInterruptingNodeFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        InterruptingNode bo = (InterruptingNode) getBusinessObjectForPictogramElement(pe);
        String imageId = getImageId(bo);
        if (!Objects.equal(((Image) pe.getGraphicsAlgorithm()).getId(), imageId)) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        InterruptingNode bo = (InterruptingNode) getBusinessObjectForPictogramElement(pe);
        String imageId = getImageId(bo);
        Image oldImage = (Image) pe.getGraphicsAlgorithm();
        IGaService gaService = Graphiti.getGaService();
        Image newImage = gaService.createImage(pe, imageId);
        gaService.setLocationAndSize(newImage, oldImage.getX(), oldImage.getY(), oldImage.getWidth(), oldImage.getHeight());
        return true;
    }

    private String getImageId(InterruptingNode node) {
        GraphElement parent = node.getParent();
        String imageName = (parent instanceof ITimed ? "boundary_" : "") + node.getTypeDefinition().getIcon();
        String imageId = "graph/" + imageName;
        if (!node.isInterrupting()) {
            imageId = IconUtil.getIconNameNotInterrupting(imageId);
        }
        return imageId;
    }
}

package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.bpmn.StartTextDecoration.StartDefinitionUI;

import com.google.common.base.Objects;

public class UpdateStartNodeFeature extends UpdateFeatureWithTextDecorator {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        StartState bo = (StartState) getBusinessObjectForPictogramElement(pe);
        StartDefinitionUI definition = (StartDefinitionUI) bo.getTextDecoratorEmulation().getDefinition().getUiContainer();
        if (!Objects.equal(definition.getSwimlaneName(), bo.getSwimlaneLabel())) {
            return Reason.createTrueReason();
        }
        if (!Objects.equal(definition.getName(), bo.getName())) {
            return Reason.createTrueReason();
        }
        if (!Objects.equal(((Image) pe.getGraphicsAlgorithm()).getId(), getImageId(bo))) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        super.update(context);
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        StartState startState = (StartState) getBusinessObjectForPictogramElement(containerShape);
        String imageId = getImageId(startState);
        if (!Objects.equal(((Image) containerShape.getGraphicsAlgorithm()).getId(), imageId)) {
            Image oldImage = (Image) containerShape.getGraphicsAlgorithm();
            Image newImage = Graphiti.getGaService().createImage(containerShape, imageId);
            Graphiti.getGaService().setLocationAndSize(newImage, oldImage.getX(), oldImage.getY(), oldImage.getWidth(), oldImage.getHeight());
        }
        return true;
    }

    private String getImageId(StartState startState) {
        ProcessDefinition definition = startState.getProcessDefinition();
        if (definition instanceof SubprocessDefinition && ((SubprocessDefinition) definition).isTriggeredByEvent() && startState.isStartByEvent()) {
            return "graph/" + startState.getEventType().getNonInterruptingImageName();
        } else {
            return "graph/" + startState.getEventType().getImageName();
        }
    }
}

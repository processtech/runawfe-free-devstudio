package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.lang.model.StartState;
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
        return Reason.createFalseReason();
    }

}

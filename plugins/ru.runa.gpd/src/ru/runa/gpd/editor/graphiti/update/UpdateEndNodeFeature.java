package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.lang.model.bpmn.AbstractEndTextDecorated;
import ru.runa.gpd.lang.model.bpmn.EndTextDecoration.EndDefinitionUI;

public class UpdateEndNodeFeature extends UpdateFeatureWithTextDecorator {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        AbstractEndTextDecorated bo = (AbstractEndTextDecorated) getBusinessObjectForPictogramElement(pe);
        EndDefinitionUI definition = (EndDefinitionUI) bo.getTextDecoratorEmulation().getDefinition().getUiContainer();
        if (!Objects.equal(definition.getName(), bo.getName())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

}

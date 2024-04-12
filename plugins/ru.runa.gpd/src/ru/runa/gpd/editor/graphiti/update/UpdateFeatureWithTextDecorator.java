package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.UIContainer;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.bpmn.StartTextDecoration.StartDefinitionUI;

public class UpdateFeatureWithTextDecorator extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        HasTextDecorator bo = (HasTextDecorator) getBusinessObjectForPictogramElement(pe);
        UIContainer definition = bo.getTextDecoratorEmulation().getDefinition().getUiContainer();
        if (!Objects.equal(definition.getName(), ((NamedGraphElement) bo).getName())) {
            return Reason.createTrueReason();
        }
        if (bo instanceof SwimlanedNode && definition instanceof StartDefinitionUI
                && !Objects.equal(((StartDefinitionUI) definition).getSwimlaneName(), ((SwimlanedNode) bo).getSwimlaneLabel())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {

        PictogramElement pe = context.getPictogramElement();

        HasTextDecorator node = (HasTextDecorator) getBusinessObjectForPictogramElement(pe);
        node.getTextDecoratorEmulation().getDefinition().getUiContainer().update();

        return true;
    }

}

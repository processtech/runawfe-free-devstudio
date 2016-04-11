package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;

abstract public class UpdateFeatureWithTextDecorator extends UpdateFeature {

    @Override
    public boolean update(IUpdateContext context) {

        PictogramElement pe = context.getPictogramElement();

        HasTextDecorator node = (HasTextDecorator) getBusinessObjectForPictogramElement(pe);
        node.getTextDecoratorEmulation().getDefinition().getUiContainer().update();

        return true;
    }

}

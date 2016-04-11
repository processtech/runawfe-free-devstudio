package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.AbstractEndTextDecorated;

import com.google.common.base.Objects;

public class UpdateEndNodeFeature extends UpdateFeatureWithTextDecorator {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        AbstractEndTextDecorated bo = (AbstractEndTextDecorated) getBusinessObjectForPictogramElement(pe);
        String name = PropertyUtil.findTextValueRecursive(pe, GaProperty.NAME);

        if (!Objects.equal(name, bo.getName())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

}

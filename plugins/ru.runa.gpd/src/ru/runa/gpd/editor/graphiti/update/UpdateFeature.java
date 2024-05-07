package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.PropertyUtil;

public abstract class UpdateFeature extends AbstractUpdateFeature {
    private DiagramFeatureProvider featureProvider;

    public UpdateFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public boolean canUpdate(IUpdateContext context) {
        return validContext(context);
    }

    @Override
    public boolean hasDoneChanges() {
        return false;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private boolean validContext(IContext context) {
        if (context instanceof IUpdateContext) {
            PictogramElement pe = ((IUpdateContext) context).getPictogramElement();
            if (pe != null) {
                Object bo = getBusinessObjectForPictogramElement(pe);
                if (bo != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canUndo(IContext context) {
        return validContext(context);
    }

    protected boolean propertiesDiffer(PictogramElement pe, String gaProperty, String property) {
        if (PropertyUtil.getProperty(pe, gaProperty) == null) { // No such property
            return false;
		}
		String propertyValue = PropertyUtil.getPropertyValue(pe, gaProperty);
		return !Objects.equal(propertyValue, property);
	}

    protected boolean textPropertiesDiffer(PictogramElement pe, String gaProperty, String property) {
        if (PropertyUtil.findGaRecursiveByName(pe, gaProperty) == null) { // No such property
            return false;
        }
        String propertyValue = PropertyUtil.findTextValueRecursive(pe, gaProperty);
        return !Objects.equal(propertyValue, property);
    }

}

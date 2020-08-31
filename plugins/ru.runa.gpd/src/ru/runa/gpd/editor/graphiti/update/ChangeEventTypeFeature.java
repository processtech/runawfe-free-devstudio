package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.EventNodeType;

public class ChangeEventTypeFeature extends AbstractCustomFeature {

    private EventNodeType newType;

    public ChangeEventTypeFeature(IFeatureProvider fp, EventNodeType newType) {
        super(fp);
        this.newType = newType;
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return getFeatureProvider().getBusinessObjectForPictogramElement(context.getPictogramElements()[0]) instanceof AbstractEventNode;
    }

    @Override
    public void execute(ICustomContext context) {
        AbstractEventNode node = (AbstractEventNode) getFeatureProvider().getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
        node.setEventNodeType(newType);
    }
}

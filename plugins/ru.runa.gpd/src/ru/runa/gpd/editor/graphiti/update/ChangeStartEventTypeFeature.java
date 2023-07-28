package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.bpmn.StartEventType;

public class ChangeStartEventTypeFeature extends AbstractCustomFeature {

    private StartEventType newType;

    public ChangeStartEventTypeFeature(IFeatureProvider fp, StartEventType newType) {
        super(fp);
        this.newType = newType;
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return getFeatureProvider().getBusinessObjectForPictogramElement(context.getPictogramElements()[0]) instanceof StartState;
    }

    @Override
    public void execute(ICustomContext context) {
        StartState startNode = (StartState) getFeatureProvider().getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
        startNode.setSwimlane(null);
        if (newType == StartEventType.blank) {
            startNode.getProcessDefinition().setDefaultStartNode(startNode);
        } else {
            startNode.setEventType(newType);
        }
        startNode.deleteFiles();
    }
}

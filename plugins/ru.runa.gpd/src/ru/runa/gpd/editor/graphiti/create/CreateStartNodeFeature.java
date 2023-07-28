package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.bpmn.StartEventType;

public class CreateStartNodeFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (parentObject instanceof Subprocess) {
            return false;
        }
        return super.canCreate(context);
    }

    @Override
    public Object[] create(ICreateContext context) {
        Object[] newObjects = super.create(context);
        if (newObjects != null) {
            StartState newStartNode = (StartState) newObjects[0];
            ProcessDefinition definition = newStartNode.getProcessDefinition();
            if (definition instanceof SubprocessDefinition && ((SubprocessDefinition) definition).isTriggeredByEvent()) {
                newStartNode.setEventType(StartEventType.message);
            }
        }
        return newObjects;
    }
}

package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.util.VariableUtils;

public class CreateSwimlaneFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return parentObject instanceof ProcessDefinition;
    }

    @Override
    public Object[] create(ICreateContext context) {
        Object[] result = super.create(context);
        for (Object object : result) {
            Swimlane swimlane = (Swimlane) object;
            swimlane.setScriptingName(VariableUtils.generateNameForScripting(swimlane, swimlane.getName(), swimlane));
        }
        return result;
    }
}

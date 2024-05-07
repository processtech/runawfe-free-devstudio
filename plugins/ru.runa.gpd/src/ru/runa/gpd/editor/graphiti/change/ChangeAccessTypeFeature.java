package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class ChangeAccessTypeFeature extends ChangePropertyFeature<ProcessDefinition, ProcessDefinitionAccessType> {

    public ChangeAccessTypeFeature(ProcessDefinition target, ProcessDefinitionAccessType newValue) {
        super(target, target.getAccessType(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setAccessType(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setAccessType(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("ProcessDefinition.property.accessType");
    }

}

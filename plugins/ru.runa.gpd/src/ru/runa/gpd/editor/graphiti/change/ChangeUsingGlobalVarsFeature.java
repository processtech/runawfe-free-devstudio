package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class ChangeUsingGlobalVarsFeature extends ChangePropertyFeature<ProcessDefinition, Boolean> {

    public ChangeUsingGlobalVarsFeature(ProcessDefinition target, Boolean newValue) {
        super(target, target.isUsingGlobalVars(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setUsingGlobalVars(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setUsingGlobalVars(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("ProcessDefinition.property.useGlobals");
    }

}

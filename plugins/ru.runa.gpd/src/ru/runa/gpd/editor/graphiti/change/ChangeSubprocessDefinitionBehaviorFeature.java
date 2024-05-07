package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.EndTokenSubprocessDefinitionBehavior;

public class ChangeSubprocessDefinitionBehaviorFeature extends ChangePropertyFeature<EndTokenState, EndTokenSubprocessDefinitionBehavior> {

    public ChangeSubprocessDefinitionBehaviorFeature(EndTokenState target, EndTokenSubprocessDefinitionBehavior newValue) {
        super(target, target.getSubprocessDefinitionBehavior(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setSubprocessDefinitionBehavior(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setSubprocessDefinitionBehavior(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("EndTokenState.property.behaviour");
    }

}

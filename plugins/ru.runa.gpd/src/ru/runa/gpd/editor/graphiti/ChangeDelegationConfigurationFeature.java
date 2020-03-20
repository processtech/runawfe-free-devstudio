package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Delegable;

public class ChangeDelegationConfigurationFeature extends ChangePropertyFeature<Delegable, String> {

    public ChangeDelegationConfigurationFeature(Delegable target, String newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setDelegationConfiguration(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setDelegationConfiguration(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getDelegationConfiguration();
        target.setDelegationConfiguration(newValue);
    }

}

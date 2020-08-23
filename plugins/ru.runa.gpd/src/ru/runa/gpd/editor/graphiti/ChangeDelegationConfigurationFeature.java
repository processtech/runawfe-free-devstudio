package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Delegable;

public class ChangeDelegationConfigurationFeature extends ChangePropertyFeature<Delegable, String> {

    public ChangeDelegationConfigurationFeature(Delegable target, String newValue) {
        this(target, target.getDelegationConfiguration(), newValue);
    }

    public ChangeDelegationConfigurationFeature(Delegable target, String oldValue, String newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setDelegationConfiguration(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDelegationConfiguration(newValue);
    }

}

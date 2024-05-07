package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.DelegationConfiguration;

public class ChangeDelegationConfigurationFeature extends ChangePropertyFeature<DelegationConfiguration, String> {

    public ChangeDelegationConfigurationFeature(DelegationConfiguration target, String newValue) {
        super(target, target.getDelegationConfiguration(), newValue);
    }

    public ChangeDelegationConfigurationFeature(DelegationConfiguration target, String oldValue, String newValue) {
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

    @Override
    public String getName() {
        return Localization.getString("label.action.editConfiguration");
    }

}

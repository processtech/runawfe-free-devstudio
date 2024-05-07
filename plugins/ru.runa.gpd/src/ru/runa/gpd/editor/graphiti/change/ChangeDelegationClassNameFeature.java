package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.DelegationConfiguration;

public class ChangeDelegationClassNameFeature extends ChangePropertyFeature<DelegationConfiguration, String> {

    private String oldConfiguration;

    public ChangeDelegationClassNameFeature(DelegationConfiguration target, String newValue) {
        this(target, target.getDelegationClassName(), newValue);
    }

    public ChangeDelegationClassNameFeature(DelegationConfiguration target, String oldValue, String newValue) {
        super(target, oldValue, newValue);
        oldConfiguration = target.getDelegationConfiguration();
    }

    @Override
    protected void undo(IContext context) {
        target.setDelegationClassName(oldValue);
        target.setDelegationConfiguration(oldConfiguration);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDelegationClassName(newValue);
        target.setDelegationConfiguration(null);
    }

    @Override
    public String getName() {
        return Localization.getString("property.delegation.class");
    }

}

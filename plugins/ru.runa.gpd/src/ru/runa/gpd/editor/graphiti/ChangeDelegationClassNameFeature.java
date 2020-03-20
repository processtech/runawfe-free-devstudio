package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Delegable;

public class ChangeDelegationClassNameFeature extends ChangePropertyFeature<Delegable, String> {

    private String oldConfiguration;

    public ChangeDelegationClassNameFeature(Delegable target, String newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setDelegationClassName(oldValue);
        target.setDelegationConfiguration(oldConfiguration);
    }

    @Override
    public void postRedo(IContext context) {
        target.setDelegationClassName(newValue);
        target.setDelegationConfiguration(null);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getDelegationClassName();
        oldConfiguration = target.getDelegationConfiguration();
        target.setDelegationClassName(newValue);
        target.setDelegationConfiguration(null);
    }

}

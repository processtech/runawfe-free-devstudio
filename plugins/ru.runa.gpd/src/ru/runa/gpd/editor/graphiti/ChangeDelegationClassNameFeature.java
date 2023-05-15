package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class ChangeDelegationClassNameFeature extends ChangePropertyFeature<Delegable, String> {

    private String oldConfiguration;

    public ChangeDelegationClassNameFeature(Delegable target, String newValue) {
        this(target, target.getDelegationClassName(), newValue);
    }

    public ChangeDelegationClassNameFeature(Delegable target, String oldValue, String newValue) {
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
        if (target instanceof ScriptTask) {
            ((ScriptTask) target).resetNameToDefault();
        }
    }

}

package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;

public class ChangeIgnoreSubstitutionRulesFeature extends ChangePropertyFeature<TaskState, Boolean> {

    public ChangeIgnoreSubstitutionRulesFeature(TaskState target, Boolean newValue) {
        super(target, target.isIgnoreSubstitutionRules(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setIgnoreSubstitutionRules(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setIgnoreSubstitutionRules(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("property.ignoreSubstitution");
    }

}

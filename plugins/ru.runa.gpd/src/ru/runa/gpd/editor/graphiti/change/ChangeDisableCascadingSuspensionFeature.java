package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Subprocess;

public class ChangeDisableCascadingSuspensionFeature extends ChangePropertyFeature<Subprocess, Boolean> {

    public ChangeDisableCascadingSuspensionFeature(Subprocess target, Boolean newValue) {
        super(target, target.isDisableCascadingSuspension(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDisableCascadingSuspension(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setDisableCascadingSuspension(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("Subprocess.DisableCascadingSuspension");
    }

}

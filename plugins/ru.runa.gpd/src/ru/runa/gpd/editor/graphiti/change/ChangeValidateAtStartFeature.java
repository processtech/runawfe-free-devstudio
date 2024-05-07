package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Subprocess;

public class ChangeValidateAtStartFeature extends ChangePropertyFeature<Subprocess, Boolean> {

    public ChangeValidateAtStartFeature(Subprocess target, Boolean newValue) {
        super(target, target.isValidateAtStart(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setValidateAtStart(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setValidateAtStart(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("Subprocess.ValidateAtStart");
    }

}

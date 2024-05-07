package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Subprocess;

public class ChangeTransactionalFeature extends ChangePropertyFeature<Subprocess, Boolean> {

    public ChangeTransactionalFeature(Subprocess target, Boolean newValue) {
        super(target, target.isTransactional(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setTransactional(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setTransactional(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("Subprocess.Transactional");

    }

}

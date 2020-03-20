package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Describable;

public class ChangeDescriptionFeature extends ChangePropertyFeature<Describable, String> {

    public ChangeDescriptionFeature(Describable target, String newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setDescription(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setDescription(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getDescription();
        target.setDescription(newValue);
    }

}

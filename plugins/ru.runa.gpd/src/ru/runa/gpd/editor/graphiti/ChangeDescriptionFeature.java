package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Describable;

public class ChangeDescriptionFeature extends ChangePropertyFeature<Describable, String> {

    public ChangeDescriptionFeature(Describable target, String newValue) {
        this(target, target.getDescription(), newValue);
    }

    public ChangeDescriptionFeature(Describable target, String oldValue, String newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setDescription(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDescription(newValue);
    }

}

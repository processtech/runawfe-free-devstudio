package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class ChangeNameFeature extends ChangePropertyFeature<NamedGraphElement, String> {

    public ChangeNameFeature(NamedGraphElement target, String newValue) {
        this(target, target.getName(), newValue);
    }

    public ChangeNameFeature(NamedGraphElement target, String oldValue, String newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setName(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setName(newValue);
    }

}

package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class ChangeNameFeature extends ChangePropertyFeature<NamedGraphElement, String> {

    public ChangeNameFeature(NamedGraphElement target, String newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setName(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setName(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getName();
        target.setName(newValue);
    }

}

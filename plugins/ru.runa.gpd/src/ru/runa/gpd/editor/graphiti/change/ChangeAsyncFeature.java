package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Synchronizable;

public class ChangeAsyncFeature extends ChangePropertyFeature<Synchronizable, Boolean> {

    protected ChangeAsyncFeature(Synchronizable target, Boolean oldValue, Boolean newValue) {
        super(target, oldValue, newValue);
    }

    public ChangeAsyncFeature(Synchronizable target, boolean newValue) {
        this(target, target.isAsync(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setAsync(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setAsync(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("label.action.feature.async");
    }

}

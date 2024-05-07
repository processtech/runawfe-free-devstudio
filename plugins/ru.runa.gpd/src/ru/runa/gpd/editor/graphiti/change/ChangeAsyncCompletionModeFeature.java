package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Synchronizable;
import ru.runa.wfe.lang.AsyncCompletionMode;

public class ChangeAsyncCompletionModeFeature extends ChangePropertyFeature<Synchronizable, AsyncCompletionMode> {

    protected ChangeAsyncCompletionModeFeature(Synchronizable target, AsyncCompletionMode oldValue, AsyncCompletionMode newValue) {
        super(target, oldValue, newValue);
    }

    public ChangeAsyncCompletionModeFeature(Synchronizable target, AsyncCompletionMode newValue) {
        this(target, target.getAsyncCompletionMode(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setAsyncCompletionMode(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setAsyncCompletionMode(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("label.action.asyncCompletionMode");
    }

}

package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Node;

public class ChangeCompactViewFeature extends ChangePropertyFeature<Node, Boolean> {

    protected ChangeCompactViewFeature(Node target, Boolean oldValue, Boolean newValue) {
        super(target, oldValue, newValue);
    }

    public ChangeCompactViewFeature(Node target, boolean newValue) {
        this(target, target.isMinimizedView(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setMinimizedView(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setMinimizedView(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("label.action.feature.compactView");
    }

}

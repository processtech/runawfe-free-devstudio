package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.NodeAsyncExecution;

public class ChangeAsyncExecutionFeature extends ChangePropertyFeature<Node, NodeAsyncExecution> {

    public ChangeAsyncExecutionFeature(Node target, NodeAsyncExecution newValue) {
        super(target, target.getAsyncExecution(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setAsyncExecution(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setAsyncExecution(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("Node.property.asyncExecution");
    }

}

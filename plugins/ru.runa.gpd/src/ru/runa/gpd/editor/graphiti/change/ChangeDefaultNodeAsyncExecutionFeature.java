package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.NodeAsyncExecution;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class ChangeDefaultNodeAsyncExecutionFeature extends ChangePropertyFeature<ProcessDefinition, NodeAsyncExecution> {

    public ChangeDefaultNodeAsyncExecutionFeature(ProcessDefinition target, NodeAsyncExecution newValue) {
        super(target, target.getDefaultNodeAsyncExecution(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDefaultNodeAsyncExecution(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setDefaultNodeAsyncExecution(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("ProcessDefinition.property.nodeAsyncExecution");
    }

}

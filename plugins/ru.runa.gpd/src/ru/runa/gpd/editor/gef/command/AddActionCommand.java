package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;
import ru.runa.gpd.lang.model.jpdl.ActionImpl;

public class AddActionCommand extends Command {
    private ActionContainer target;
    private Action action;
    private int actionIndex = -1;

    @Override
    public void execute() {
        action = NodeRegistry.getNodeTypeDefinition(ActionImpl.class).createElement((GraphElement) target, true);
        target.addAction(action, actionIndex);
    }

    @Override
    public void undo() {
        target.removeAction(action);
    }

    public void setTarget(ActionContainer newTarget) {
        target = newTarget;
    }

    public Action getAction() {
        return action;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }
}

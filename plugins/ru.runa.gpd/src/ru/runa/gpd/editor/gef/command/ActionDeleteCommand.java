package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class ActionDeleteCommand extends Command {
    private Action action;
    private ActionContainer parent;
    private int index;

    @Override
    public void execute() {
        parent = (ActionContainer) action.getParent();
        index = parent.removeAction(action);
    }

    @Override
    public void undo() {
        parent.addAction(action, index);
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

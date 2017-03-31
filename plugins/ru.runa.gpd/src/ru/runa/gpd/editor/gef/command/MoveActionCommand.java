package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class MoveActionCommand extends Command {
    private final ActionContainer newTarget;
    private final int newIndex;
    private final ActionContainer oldTarget;
    private int oldIndex;
    private int oldIndexesCount;
    private final Action action;

    public MoveActionCommand(ActionContainer newTarget, Action action, int newIndex) {
        this.newTarget = newTarget;
        this.newIndex = newIndex;
        this.oldTarget = (ActionContainer) action.getParent();
        this.oldIndex = oldTarget.getActions().indexOf(action);
        this.oldIndexesCount = oldTarget.getActions().size();
        this.action = action;
    }
    
    @Override
    public boolean canExecute() {
        if (newTarget==oldTarget && (newIndex==oldIndex || (newIndex==-1 && oldIndexesCount-1==oldIndex))) {
            return false;
        }
        return true;
    }
    
    @Override
    public void execute() {
        oldTarget.removeAction(action);
        newTarget.addAction(action, newIndex);
    }

    @Override
    public void undo() {
        newTarget.removeAction(action);
        oldTarget.addAction(action, oldIndex);
    }

}

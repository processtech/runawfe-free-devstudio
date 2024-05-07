package ru.runa.gpd.ui.custom;

import org.eclipse.gef.commands.CommandStack;

public class ControlUndoRedoListener extends AbstractUndoRedoListener {

    private final CommandStack commandStack;

    public ControlUndoRedoListener(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    @Override
    protected void undo() {
        commandStack.undo();
    }

    @Override
    protected void redo() {
        commandStack.redo();
    }

}

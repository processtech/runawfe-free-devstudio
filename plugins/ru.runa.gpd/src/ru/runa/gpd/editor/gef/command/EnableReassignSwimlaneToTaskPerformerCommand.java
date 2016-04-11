package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.TaskState;

public class EnableReassignSwimlaneToTaskPerformerCommand extends Command {
    private final TaskState taskState;

    public EnableReassignSwimlaneToTaskPerformerCommand(TaskState taskState) {
        this.taskState = taskState;
    }

    @Override
    public void execute() {
        taskState.setReassignSwimlaneToTaskPerformer(!taskState.isReassignSwimlaneToTaskPerformer());
    }

    @Override
    public void undo() {
        taskState.setReassignSwimlaneToTaskPerformer(!taskState.isReassignSwimlaneToTaskPerformer());
    }

}

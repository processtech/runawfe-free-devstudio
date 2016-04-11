package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.TaskState;

public class EnableReassignSwimlaneToInitializerCommand extends Command {
    private final TaskState taskState;

    public EnableReassignSwimlaneToInitializerCommand(TaskState taskState) {
        this.taskState = taskState;
    }

    @Override
    public void execute() {
        taskState.setReassignSwimlaneToInitializerValue(!taskState.isReassignSwimlaneToInitializerValue());
    }

    @Override
    public void undo() {
        taskState.setReassignSwimlaneToInitializerValue(!taskState.isReassignSwimlaneToInitializerValue());
    }

}

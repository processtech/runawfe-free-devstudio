package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

public class ProcessDefinitionRemoveSwimlaneCommand extends Command {
    private ProcessDefinition definition;

    private Swimlane swimlane;

    public void setProcessDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    @Override
    public void execute() {
        definition.removeChild(swimlane);
    }

    @Override
    public void undo() {
        definition.addChild(swimlane);
    }
}

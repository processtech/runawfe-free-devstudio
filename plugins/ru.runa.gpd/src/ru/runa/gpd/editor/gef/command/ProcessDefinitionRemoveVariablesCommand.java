package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public class ProcessDefinitionRemoveVariablesCommand extends Command {

    private Variable variable;

    private ProcessDefinition definition;

    @Override
    public void execute() {
        definition.removeChild(variable);
    }

    @Override
    public void undo() {
        definition.addChild(variable);
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public void setProcessDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

}

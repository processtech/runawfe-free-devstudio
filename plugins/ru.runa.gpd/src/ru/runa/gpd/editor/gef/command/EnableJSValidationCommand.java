package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.FormNode;

public class EnableJSValidationCommand extends Command {

    private FormNode formNode;

    private boolean enabled;

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute() {
        formNode.setUseJSValidation(enabled);
    }

    @Override
    public void undo() {
        formNode.setUseJSValidation(!formNode.isUseJSValidation());
    }

}

package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.FormNode;

public class FormNodeSetFileCommand extends Command {

    private FormNode formNode;

    private String fileName;

    private String oldFileName;

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void execute() {
        oldFileName = formNode.getFormFileName();
        formNode.setFormFileName(fileName);
    }

    @Override
    public void undo() {
        formNode.setFormFileName(oldFileName);
    }

}

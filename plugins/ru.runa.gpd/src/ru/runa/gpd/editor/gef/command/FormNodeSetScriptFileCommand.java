package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.FormNode;

public class FormNodeSetScriptFileCommand extends Command {

    private FormNode formNode;

    private String scriptFileName;

    private String oldFileName;

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }
    
    @Override
    public void execute() {
        oldFileName = formNode.getScriptFileName();
        formNode.setScriptFileName(scriptFileName);
    }

    @Override
    public void undo() {
        formNode.setScriptFileName(oldFileName);
    }

}

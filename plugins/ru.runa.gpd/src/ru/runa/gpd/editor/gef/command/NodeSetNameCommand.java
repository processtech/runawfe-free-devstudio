package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.Node;

public class NodeSetNameCommand extends Command {
    private String oldName;
    private String newName;
    private Node node;

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        newName = name;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public void execute() {
        oldName = node.getName();
        node.setName(newName);
    }

    @Override
    public void undo() {
        node.setName(oldName);
    }
}

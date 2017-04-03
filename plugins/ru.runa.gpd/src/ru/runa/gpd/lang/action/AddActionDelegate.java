package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.editor.gef.command.AddActionCommand;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class AddActionDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        AddActionCommand command = new AddActionCommand();
        command.setTarget((ActionContainer) getSelection());
        executeCommand(command);
    }
}

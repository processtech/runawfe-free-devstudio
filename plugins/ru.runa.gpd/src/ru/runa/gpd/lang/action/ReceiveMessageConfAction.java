package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.ReceiveMessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

public class ReceiveMessageConfAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        ReceiveMessageNode messageNode = getSelection();
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(), false);
        if (dialog.open() != Window.CANCEL) {
            messageNode.setVariableMappings(dialog.getVariableMappings());
        }
    }
}

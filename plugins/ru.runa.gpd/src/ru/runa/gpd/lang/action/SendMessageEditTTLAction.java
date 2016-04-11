package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.SendMessageNode;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class SendMessageEditTTLAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        SendMessageNode messageNode = getSelection();
        DurationEditDialog dialog = new DurationEditDialog(messageNode.getProcessDefinition(), messageNode.getTtlDuration());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
            messageNode.setTtlDuration(result);
        }
    }
}

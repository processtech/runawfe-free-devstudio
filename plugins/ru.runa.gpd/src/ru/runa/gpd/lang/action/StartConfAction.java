package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;
import ru.runa.gpd.ui.dialog.StartStateTimerDialog;

public class StartConfAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        StartState startNode = getSelection();
        if (startNode.isStartByEvent()) {
            if (startNode.isStartByTimer()) {
                String newTimerDefinition = new StartStateTimerDialog(startNode.getTimerEventDefinition()).openDialog();
                if (newTimerDefinition != null) {
                    startNode.setTimerEventDefinition(newTimerDefinition);
                }
            } else {
                MessageNodeDialog dialog = new MessageNodeDialog(startNode.getProcessDefinition(), startNode.getVariableMappings(), false,
                        startNode.getName());
                if (dialog.open() != Window.CANCEL) {
                    startNode.setVariableMappings(dialog.getVariableMappings());
                }
            }
        }
    }

}

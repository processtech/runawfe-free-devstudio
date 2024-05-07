package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.graphiti.change.ChangeTimerActionFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class EditTimerActionAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        Timer timer = getSelection();
        TimerAction oldAction = timer.getAction();
        TimerActionEditDialog dialog = new TimerActionEditDialog(timer.getProcessDefinition(), timer.getAction());
        TimerAction result = (TimerAction) dialog.openDialog();
        if (result != null) {
            if (!Objects.equal(result, oldAction)) {
                UndoRedoUtil.executeFeature(new ChangeTimerActionFeature(timer, result));
            }
        }
    }
}

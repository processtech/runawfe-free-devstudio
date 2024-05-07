package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.graphiti.change.ChangeTimerDelayFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class EditTimerDurationAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        Timer timer = getSelection();
        Duration oldDelay = timer.getDelay();
        DurationEditDialog dialog = new DurationEditDialog(timer.getProcessDefinition(), timer.getDelay());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
            if (!Objects.equal(result, oldDelay)) {
                UndoRedoUtil.executeFeature(new ChangeTimerDelayFeature(timer, result));
            }
        }
    }
}

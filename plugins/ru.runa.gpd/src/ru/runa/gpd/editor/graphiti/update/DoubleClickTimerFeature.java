package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.editor.graphiti.ChangeTimerActionFeature;
import ru.runa.gpd.editor.graphiti.UndoRedoUtil;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class DoubleClickTimerFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return getBusinessObject(context) instanceof Timer && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        Timer timer = (Timer) getBusinessObject(context);
        TimerAction oldAction = timer.getAction();
        TimerActionEditDialog dialog = new TimerActionEditDialog(timer.getProcessDefinition(), timer.getAction());
        TimerAction result = (TimerAction) dialog.openDialog();
        if (result != null) {
            if (!Objects.equal(result, oldAction)) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UndoRedoUtil.executeFeature(new ChangeTimerActionFeature(timer, result));
                    }
                });
            }
        }
    }

}

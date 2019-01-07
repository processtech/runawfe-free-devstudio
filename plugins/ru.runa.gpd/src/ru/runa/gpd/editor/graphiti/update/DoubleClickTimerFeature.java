package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class DoubleClickTimerFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement()) instanceof Timer && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        Timer timer = (Timer) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        TimerActionEditDialog dialog = new TimerActionEditDialog(timer.getProcessDefinition(), timer.getAction());
        TimerAction result = (TimerAction) dialog.openDialog();
        if (result != null) {
            timer.setAction(result);
        }
    }

}

package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class DoubleClickTimerFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return getBusinessObject(context) instanceof Timer && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        Timer timer = (Timer) getBusinessObject(context);
        DurationEditDialog dialog = new DurationEditDialog(timer.getProcessDefinition(), timer.getDelay());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
        	timer.setDelay(result);
        }
    }

}

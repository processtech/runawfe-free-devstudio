package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;

public class TimerFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        ITimed timed = (ITimed) getSelection();
        if (timed != null) {
            action.setChecked(timed.getTimer() != null);
        }
    }

    @Override
    public void run(IAction action) {
        ITimed timed = (ITimed) getSelection();
        Timer timer = timed.getTimer();
        if (timer != null) {
            getSelection().removeChild(timer);
        } else {
            getSelection().addChild(new Timer());
        }
    }
}

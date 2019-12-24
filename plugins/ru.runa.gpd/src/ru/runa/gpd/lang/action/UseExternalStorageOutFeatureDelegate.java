package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class UseExternalStorageOutFeatureDelegate extends AbstractUseExternalStorageFeatureDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        final ScriptTask scriptTask = getSelection();
        if (scriptTask != null) {
            action.setChecked(scriptTask.isUseExternalStorageOut());
        }
    }

    @Override
    protected boolean changeProperty(ScriptTask scriptTask) {
        scriptTask.setUseExternalStorageOut(!scriptTask.isUseExternalStorageOut());
        return scriptTask.isUseExternalStorageOut();
    }
}

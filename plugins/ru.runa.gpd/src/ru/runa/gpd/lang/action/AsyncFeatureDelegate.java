package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.ISynchronizable;

public class AsyncFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        GraphElement elem = getSelection();
        if (elem != null && elem instanceof Subprocess) {
            Subprocess subprocess = (Subprocess) elem;
            action.setEnabled(!subprocess.isEmbedded() || subprocess.getEmbeddedSubprocess() == null);
        }
        ISynchronizable iSynchronizable = (ISynchronizable) elem;
        if (iSynchronizable != null) {
            action.setChecked(iSynchronizable.isAsync());
        }
    }

    @Override
    public void run(IAction action) {
        ISynchronizable iSynchronizable = (ISynchronizable) getSelection();
        iSynchronizable.setAsync(!iSynchronizable.isAsync());
    }
}

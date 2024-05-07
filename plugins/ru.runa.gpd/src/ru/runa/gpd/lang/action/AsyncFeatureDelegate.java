package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.editor.graphiti.change.ChangeAsyncFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Synchronizable;

public class AsyncFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        GraphElement elem = getSelection();
        if (elem != null && elem instanceof Subprocess) {
            Subprocess subprocess = (Subprocess) elem;
            action.setEnabled(!subprocess.isEmbedded() || subprocess.getEmbeddedSubprocess() == null);
        }
        Synchronizable synchronizable = (Synchronizable) elem;
        if (synchronizable != null) {
            action.setChecked(synchronizable.isAsync());
        }
    }

    @Override
    public void run(IAction action) {
        Synchronizable synchronizable = (Synchronizable) getSelection();
        UndoRedoUtil.executeFeature(new ChangeAsyncFeature(synchronizable, !synchronizable.isAsync()));

    }
}

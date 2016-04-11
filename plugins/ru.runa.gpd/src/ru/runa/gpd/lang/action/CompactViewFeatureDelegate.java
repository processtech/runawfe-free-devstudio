package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.Node;

public class CompactViewFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        Node node = getSelection();
        if (node != null) {
            action.setChecked(node.isMinimizedView());
        }
    }

    @Override
    public void run(IAction action) {
        Node node = getSelection();
        node.setMinimizedView(!node.isMinimizedView());
    }
}

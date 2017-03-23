package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEvent;

public abstract class BaseBoundaryEventFeatureDelegate <T> extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        @SuppressWarnings("unchecked")
		T boundaryEvented = (T) getSelection();
        if (boundaryEvented != null) {
            action.setChecked( getBoundaryEvent(boundaryEvented) != null);
        }
    }

    @Override
    public void run(IAction action) {
        @SuppressWarnings("unchecked")
		T boundaryEvented = (T) getSelection();
        Node node = getBoundaryEvent(boundaryEvented);
        if (node != null) {
            getSelection().removeChild(node);
        } else {
            getSelection().addChild(createBoundaryEvent());
        }
    }

	protected abstract GraphElement createBoundaryEvent();

	protected abstract Node getBoundaryEvent(T boundaryEvented);
}

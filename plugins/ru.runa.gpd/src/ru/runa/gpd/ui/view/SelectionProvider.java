package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class SelectionProvider implements ISelectionProvider {
    private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
    private ISelection selection;

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public ISelection getSelection() {
        return selection;
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;
        fireSelectionChanged();
    }

    private void fireSelectionChanged() {
        for (ISelectionChangedListener listener : listeners) {
            listener.selectionChanged(new SelectionChangedEvent(this, selection));
        }
    }

}

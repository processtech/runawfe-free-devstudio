package ru.runa.gpd.editor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

public class StructuredSelectionProvider implements ISelectionProvider {
    private final Object selection;

    public StructuredSelectionProvider(Object selection) {
        this.selection = selection;
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    @Override
    public ISelection getSelection() {
        if (selection == null) {
            return new StructuredSelection();
        }
        return new StructuredSelection(selection);
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }

    @Override
    public void setSelection(ISelection selection) {
    }
}

package ru.runa.gpd.formeditor.ftl.ui;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;

public class ComponentDoubleClickListener implements IDoubleClickListener {
    private final TableViewer viewer;

    public ComponentDoubleClickListener(TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        ComponentType type = (ComponentType) selection.getFirstElement();
        String data = "${" + type.getId() + "()}";
        FormEditor.getCurrent().insertText(data);
    }
}

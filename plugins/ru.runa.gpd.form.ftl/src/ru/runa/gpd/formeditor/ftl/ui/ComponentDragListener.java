package ru.runa.gpd.formeditor.ftl.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class ComponentDragListener implements DragSourceListener {
    private final TableViewer viewer;

    public ComponentDragListener(TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        ComponentType type = (ComponentType) selection.getFirstElement();
        if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = "${" + type.getId() + "()}";
        }
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editorPart instanceof IComponentDropTarget) {
            IComponentDropTarget componentDropTarget = ((IComponentDropTarget) editorPart);
            componentDropTarget.doDrop();
        }
    }

}
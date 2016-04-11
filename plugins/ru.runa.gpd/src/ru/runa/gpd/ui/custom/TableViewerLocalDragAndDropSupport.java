package ru.runa.gpd.ui.custom;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewerLocalDragAndDropSupport {

    public static void enable(TableViewer tableViewer, DragAndDropAdapter executor) {
        int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        tableViewer.addDragSupport(dndOperations, transfers, new DragElementsListener());
        tableViewer.addDropSupport(dndOperations, transfers, new DropElementsListener(tableViewer, executor));
    }

    public static class DragElementsListener extends DragSourceAdapter {

        @Override
        public void dragSetData(DragSourceEvent event) {
            event.data = "firstElement";
        }
    }

    public static class DropElementsListener extends ViewerDropAdapter {
        private final DragAndDropAdapter dragAndDropAdapter;

        public DropElementsListener(TableViewer tableViewer, DragAndDropAdapter executor) {
            super(tableViewer);
            this.dragAndDropAdapter = executor;
        }

        @Override
        public void drop(DropTargetEvent event) {
            IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
            Object target = determineTarget(event);
            if (target != null) {
                dragAndDropAdapter.onDrop(target, selection.toList());
            }
            super.drop(event);
        }

        @Override
        public boolean performDrop(Object data) {
            return false;
        }

        @Override
        public boolean validateDrop(Object target, int operation, TransferData transferType) {
            return true;
        }
    }

}

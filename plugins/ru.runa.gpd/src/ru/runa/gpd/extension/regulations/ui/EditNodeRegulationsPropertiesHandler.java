package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;
import org.eclipse.graphiti.ui.internal.parts.IDiagramEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.editor.gef.part.graph.NodeGraphicalEditPart;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.Node;

public class EditNodeRegulationsPropertiesHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Node node = null;
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(); 
        Object editPart = ((IStructuredSelection) selection).getFirstElement();
        if (editPart instanceof NodeGraphicalEditPart) {
            node = (Node) ((NodeGraphicalEditPart) editPart).getModel();
        } else if (editPart instanceof ContainerShapeEditPart && !(editPart instanceof IDiagramEditPart)) {
            node = (Node) ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).translateSelection(selection); 
        }
        if (node != null) {
            new EditNodeRegulationsPropertiesDialog(node).open();
        }
        return null;
    }

}

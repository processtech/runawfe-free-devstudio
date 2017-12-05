package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;
import org.eclipse.graphiti.ui.internal.parts.IDiagramEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.editor.gef.part.graph.NodeGraphicalEditPart;
import ru.runa.gpd.settings.CommonPreferencePage;

import com.google.common.base.Objects;

public class EditNodeRegulationsPropertiesTester extends PropertyTester {

    public static final String PROPERTY_NAMESPACE = "ru.runa.gpd.extension.regulations.ui";
    public static final String PROPERTY_REGULATIONS_ENABLED = "regulationsEnabled";
    public static final String PROPERTY_CAN_EDIT_NODE_REGULATIONS = "canEditNodeRegulations";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (PROPERTY_REGULATIONS_ENABLED.equals(property)) {
            return Objects.equal(expectedValue, CommonPreferencePage.isRegulationsMenuItemsEnabled());
        } else if (PROPERTY_CAN_EDIT_NODE_REGULATIONS.equals(property)) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                ISelection selection = window.getSelectionService().getSelection();
                if (selection != null) {
                    Object editPart = ((IStructuredSelection) selection).getFirstElement();
                    return editPart instanceof NodeGraphicalEditPart || editPart instanceof ContainerShapeEditPart && !(editPart instanceof IDiagramEditPart);
                }
            }
        }
        return false;
    }

}

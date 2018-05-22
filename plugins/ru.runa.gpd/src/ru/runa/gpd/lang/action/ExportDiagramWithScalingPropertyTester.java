package ru.runa.gpd.lang.action;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.graphiti.ui.internal.parts.IDiagramEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.settings.CommonPreferencePage;

public class ExportDiagramWithScalingPropertyTester extends PropertyTester {
    public static final String PROPERTY_EXPORT_DIAGRAM_WITH_SCALING_ENABLED = "exportDiagramWithScalingActionEnabled";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (PROPERTY_EXPORT_DIAGRAM_WITH_SCALING_ENABLED.equals(property)) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                ISelection selection = window.getSelectionService().getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object editPart = ((IStructuredSelection) selection).getFirstElement();
                    return editPart instanceof IDiagramEditPart && CommonPreferencePage.isExportWithScalingEnabled();
                }
            }
        }
        return false;
    }

}

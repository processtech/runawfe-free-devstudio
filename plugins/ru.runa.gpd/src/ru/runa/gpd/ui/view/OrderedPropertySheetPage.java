package ru.runa.gpd.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;
import ru.runa.gpd.lang.model.GraphElement;

public class OrderedPropertySheetPage extends PropertySheetPage implements PropertyChangeListener {

    private GraphElement propertySource;

    public OrderedPropertySheetPage() {
        super();
        setSorter(new PropertySheetSorter() {
            @Override
            public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB) {
                return 1; // Do not changes the logical order of properties. Prevents default alphabetical sorting.
            }
        });
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // 3274#note-8
        Menu menu = getControl().getMenu();
        MenuManager menuManager = (MenuManager) menu.getData(MenuManager.MANAGER_KEY);
        menuManager.remove("defaults");
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (propertySource != null) {
            propertySource.removePropertyChangeListener(this);
        }
        super.selectionChanged(part, selection);
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof ContainerShapeEditPart) {
                ContainerShapeEditPart container = (ContainerShapeEditPart) element;
                Object bo = container.getFeatureProvider().getBusinessObjectForPictogramElement(container.getPictogramElement());
                if (bo instanceof GraphElement) {
                    propertySource = (GraphElement) bo;
                    propertySource.addPropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }
}

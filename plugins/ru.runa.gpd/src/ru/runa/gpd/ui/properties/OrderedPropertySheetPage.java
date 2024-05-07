package ru.runa.gpd.ui.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.properties.UndoablePropertySheetPage;
import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetSorter;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.custom.ControlUndoRedoListener;

public class OrderedPropertySheetPage extends UndoablePropertySheetPage implements PropertyChangeListener {

    private GraphElement propertySource;
    private final CommandStack commandStack;

    public OrderedPropertySheetPage(CommandStack commandStack, IAction undoAction, IAction redoAction) {
        super(commandStack, undoAction, redoAction);
        this.commandStack = commandStack;
        setRootEntry(new PropertySheetEntry()); // We do not need default UndoablePropertySheetEntry because it works with EMF model
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
        getControl().addKeyListener(new ControlUndoRedoListener(commandStack));
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (propertySource != null) {
            propertySource.removePropertyChangeListener(this);
        }
        super.selectionChanged(part, selection);
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof IPictogramElementEditPart) {
                IPictogramElementEditPart container = (IPictogramElementEditPart) element;
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

package ru.runa.gpd.formeditor.ftl.ui;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ru.runa.gpd.formeditor.ftl.ComponentType;

public class FormComponentsView extends ViewPart implements IPartListener2 {
    public static final String ID = "ru.runa.gpd.formeditor.ftl.formComponentsView";
    private TableViewer viewer;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
        viewer.addDragSupport(operations, transferTypes, new ComponentDragListener(viewer));
        viewer.addDoubleClickListener(new ComponentDoubleClickListener(viewer));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(ComponentTypeContentProvider.INSTANCE.getModel());
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void partActivated(IWorkbenchPartReference arg0) {
        if (viewer != null) {
            viewer.setInput(ComponentTypeContentProvider.INSTANCE.getModel());
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference arg0) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference arg0) {
    }

    public class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // TODO palette icons support
            /*
             * ToolPalleteMethodTag todo = (ToolPalleteMethodTag) element;
             * double zoom = 1d/2; //scale to half of the size maintaining
             * aspect ratio
             * 
             * final int width = todo.getImage().getBounds().width; final int
             * height = todo.getImage().getBounds().height; return new
             * Image(Display.getDefault
             * (),todo.getImage().getImageData().scaledTo((int)(width *
             * zoom),(int)(height * zoom)));
             */
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            return ((ComponentType) element).getLabel();
        }

    }
}

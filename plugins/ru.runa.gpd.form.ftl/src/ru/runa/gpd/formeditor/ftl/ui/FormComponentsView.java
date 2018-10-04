package ru.runa.gpd.formeditor.ftl.ui;

import java.util.List;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.util.UiUtil;

public class FormComponentsView extends ViewPart implements IPartListener2 {
    public static final String ID = "ru.runa.gpd.formeditor.ftl.formComponentsView";

    private SashForm sashForm;
    private TableViewer leftViewer;
    private TableViewer rightViewer;

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
        UiUtil.hideToolBar(getViewSite());

        sashForm = new SashForm(parent, SWT.SMOOTH | SWT.HORIZONTAL);

        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };

        leftViewer = new TableViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        leftViewer.addDragSupport(operations, transferTypes, new ComponentDragListener(leftViewer));
        leftViewer.addDoubleClickListener(new ComponentDoubleClickListener(leftViewer));
        leftViewer.setContentProvider(ArrayContentProvider.getInstance());
        leftViewer.setLabelProvider(new TableLabelProvider());
        leftViewer.getTable().setHeaderVisible(true);
        TableColumn column = new TableColumn(leftViewer.getTable(), SWT.LEFT);
        column.setText(Messages.getString("form.components.header.basic"));
        column.setWidth(600);

        rightViewer = new TableViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        rightViewer.addDragSupport(operations, transferTypes, new ComponentDragListener(rightViewer));
        rightViewer.addDoubleClickListener(new ComponentDoubleClickListener(rightViewer));
        rightViewer.setContentProvider(ArrayContentProvider.getInstance());
        rightViewer.setLabelProvider(new TableLabelProvider());
        rightViewer.getTable().setHeaderVisible(true);
        column = new TableColumn(rightViewer.getTable(), SWT.LEFT);
        column.setText(Messages.getString("form.components.header.additional"));
        column.setWidth(600);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void partActivated(IWorkbenchPartReference arg0) {
        if (leftViewer != null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    List<ComponentType> model = ComponentTypeContentProvider.INSTANCE.getModel(true);
                    if (model.size() > 0) {
                        leftViewer.setInput(model);
                    } else {
                        if (!leftViewer.getTable().isDisposed()) {
                            rightViewer.getTable().setHeaderVisible(false);
                            sashForm.setMaximizedControl(rightViewer.getTable());
                        }
                    }
                }
            });
        }
        if (rightViewer != null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    List<ComponentType> model = ComponentTypeContentProvider.INSTANCE.getModel(false);
                    if (model.size() > 0) {
                        rightViewer.setInput(model);
                    } else {
                        if (!rightViewer.getTable().isDisposed()) {
                            leftViewer.getTable().setHeaderVisible(false);
                            sashForm.setMaximizedControl(leftViewer.getTable());
                        }
                    }
                }
            });
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
             * ToolPalleteMethodTag todo = (ToolPalleteMethodTag) element; double zoom = 1d/2; //scale to half of the size maintaining aspect ratio
             * 
             * final int width = todo.getImage().getBounds().width; final int height = todo.getImage().getBounds().height; return new
             * Image(Display.getDefault (),todo.getImage().getImageData().scaledTo((int)(width * zoom),(int)(height * zoom)));
             */
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            return ((ComponentType) element).getLabel();
        }

    }
}

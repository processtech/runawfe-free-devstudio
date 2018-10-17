package ru.runa.gpd.formeditor.ftl.ui;

import com.google.common.base.Strings;
import java.util.stream.Collectors;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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
    private Text filterText;
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
        UiUtil.hideToolBar(getViewSite());

        Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayout(new GridLayout());

        filterText = new Text(composite, SWT.BORDER | SWT.SEARCH);
        filterText.setMessage(Messages.getString("filter"));
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterText.addModifyListener(e -> {
            adjustComponents();
        });

        viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
        viewer.addDragSupport(operations, transferTypes, new ComponentDragListener(viewer));
        viewer.addDoubleClickListener(new ComponentDoubleClickListener(viewer));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new TableLabelProvider());
        adjustComponents();
    }

    private void adjustComponents() {
        String filter = filterText.getText().toLowerCase();
        viewer.setInput(ComponentTypeContentProvider.INSTANCE.getModel().stream()
                .filter(type -> Strings.isNullOrEmpty(filter) || type.getLabel().toLowerCase().indexOf(filter) >= 0)
                .collect(Collectors.toList()));
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void partActivated(IWorkbenchPartReference arg0) {
        if (viewer != null) {
            adjustComponents();
        }
        filterText.setFocus();
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
        filterText.setFocus();
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

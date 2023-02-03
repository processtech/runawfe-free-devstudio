package ru.runa.gpd.editor.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.PageBook;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;

public class OutlineViewer extends ContentOutlinePage implements PropertyChangeListener {
    private final static Set<String> PROPERTY_NAMES_TO_REFRESH = new HashSet<>();
    static {
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.PROPERTY_CHILDREN_CHANGED);
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.NODE_LEAVING_TRANSITION_ADDED);
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.NODE_LEAVING_TRANSITION_REMOVED);
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.PROPERTY_NAME);
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.PROPERTY_SHOW_ACTIONS);
        PROPERTY_NAMES_TO_REFRESH.add(PropertyNames.PROPERTY_CLASS);
    }
    private final ProcessEditorBase editor;
    private PageBook pageBook;
    private Control treeview;
    private Canvas overview;
    private Thumbnail thumbnail;
    private IAction showOverviewAction;
    private IAction showTreeviewAction;

    public OutlineViewer(ProcessEditorBase editor) {
        super(new FilteredTreeViewer(editor.getDefinition()));
        this.editor = editor;
    }

    @Override
    public void createControl(Composite parent) {
        createToolBar();
        createPageBook(parent);
        editor.getDefinition().setDelegatedListener(this);
    }

    @Override
    public void dispose() {
        editor.getDefinition().unsetDelegatedListener(this);
        if (null != thumbnail) {
            thumbnail.deactivate();
        }
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (PROPERTY_NAMES_TO_REFRESH.contains(event.getPropertyName())) {
            ((FilteredTreeViewer) getViewer()).refresh((GraphElement) event.getSource());
        }
    }

    private void createToolBar() {
        IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
        createShowOverviewAction(tbm);
        createShowTreeviewAction(tbm);
    }

    private void createShowOverviewAction(IToolBarManager tbm) {
        showOverviewAction = new Action() {
            @Override
            public void run() {
                showOverview();
            }
        };
        showOverviewAction.setImageDescriptor(SharedImages.getImageDescriptor("icons/overview.gif"));
        tbm.add(showOverviewAction);
    }

    private void showOverview() {
        showTreeviewAction.setChecked(false);
        showOverviewAction.setChecked(true);
        pageBook.showPage(overview);
        thumbnail.setVisible(true);
    }

    private void createShowTreeviewAction(IToolBarManager tbm) {
        showTreeviewAction = new Action() {
            @Override
            public void run() {
                showTreeview();
            }
        };
        showTreeviewAction.setImageDescriptor(SharedImages.getImageDescriptor("icons/treeview.gif"));
        tbm.add(showTreeviewAction);
    }

    private void showTreeview() {
        showTreeviewAction.setChecked(true);
        showOverviewAction.setChecked(false);
        pageBook.showPage(treeview);
        thumbnail.setVisible(false);
    }

    private void createPageBook(Composite parent) {
        pageBook = new PageBook(parent, SWT.NONE);
        createTreeview(pageBook);
        createOverview(pageBook);
        showTreeview();
    }

    private void createOverview(Composite parent) {
        ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) editor.getGraphicalViewer().getRootEditPart();
        overview = new Canvas(parent, SWT.NONE);
        LightweightSystem lws = new LightweightSystem(overview);
        thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
        thumbnail.setBorder(new MarginBorder(3));
        thumbnail.setSource(rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS));
        lws.setContents(thumbnail);
    }

    private void createTreeview(Composite parent) {
        treeview = getViewer().createControl(parent);
        getSite().setSelectionProvider(getViewer());
    }

    public void refreshTreeView() {
        ((FilteredTreeViewer) getViewer()).refresh(editor.getDefinition());
    }

    public CommandStack getCommandStack() {
        return editor.getCommandStack();
    }

    @Override
    public Control getControl() {
        return pageBook;
    }
}

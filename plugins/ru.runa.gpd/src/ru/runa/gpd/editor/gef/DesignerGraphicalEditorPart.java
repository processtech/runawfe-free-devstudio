package ru.runa.gpd.editor.gef;

import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.IObjectActionContributor;
import org.eclipse.ui.internal.ObjectActionContributorManager;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.ProcessEditorContributor;
import ru.runa.gpd.editor.StructuredSelectionProvider;
import ru.runa.gpd.lang.model.GraphElement;

@SuppressWarnings({ "unchecked", "restriction" })
public class DesignerGraphicalEditorPart extends GraphicalEditorWithFlyoutPalette implements CommandStackEventListener{
    private final ProcessEditorBase editor;
    private final DesignerPaletteRoot paletteRoot;

    public DesignerGraphicalEditorPart(ProcessEditorBase editor) {
        this.editor = editor;
        this.paletteRoot = new DesignerPaletteRoot(editor);
        setEditDomain(new DefaultEditDomain(this));
    }

    public ProcessEditorBase getEditor() {
        return editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        getSite().setSelectionProvider(editor.getSite().getSelectionProvider());
        getPaletteRoot().refreshElementsVisibility();
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getGraphicalViewer().setContents(editor.getDefinition());
    }

    @Override
    public DesignerPaletteRoot getPaletteRoot() {
        return paletteRoot;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getEditDomain().addViewer(getGraphicalViewer());
        getCommandStack().addCommandStackEventListener(this);
        getGraphicalViewer().setRootEditPart(new ScalableFreeformRootEditPart() {
            @Override
            public DragTracker getDragTracker(Request req) {
                MarqueeDragTracker tracker = (MarqueeDragTracker) super.getDragTracker(req);
                tracker.setMarqueeBehavior(MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED_AND_RELATED_CONNECTIONS);
                return tracker;
            }
        });
        getGraphicalViewer().setEditPartFactory(new EditPartFactory() {
            @Override
            public EditPart createEditPart(EditPart context, Object object) {
                if (!(object instanceof GraphElement)) {
                    return null;
                }
                GraphElement element = (GraphElement) object;
                GefEntry gefEntry = element.getTypeDefinition().getGefEntry();
                if (gefEntry != null) {
                    return gefEntry.createGraphicalEditPart(element);
                }
                throw new RuntimeException("No graph part defined for " + element);
            }
        });
        KeyHandler keyHandler = new GraphicalViewerKeyHandler(getGraphicalViewer());
        keyHandler
                .setParent(((ProcessEditorContributor) getEditor().getEditorSite().getActionBarContributor()).createKeyHandler(getActionRegistry()));
        getGraphicalViewer().setKeyHandler(keyHandler);
        getGraphicalViewer().setContextMenu(createContextMenu());
        getSite().setSelectionProvider(getGraphicalViewer());
    }

    private MenuManager createContextMenu() {
        MenuManager menuManager = new EditorContextMenuProvider(getGraphicalViewer());
        getSite().registerContextMenu("ru.runa.gpd.graph.contextmenu", menuManager, getSite().getSelectionProvider());
        return menuManager;
    }

    @Override
    protected void createActions() {
        super.createActions();
        GEFActionBarContributor.createCustomGEFActions(getActionRegistry(), editor, getSelectionActions());
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (editor.equals(getSite().getPage().getActiveEditor())) {
            updateActions(getSelectionActions());
        }
    }

    public void select(GraphElement element) {
        GraphicalEditPart target = (GraphicalEditPart) getGraphicalViewer().getEditPartRegistry().get(element);
        if (target == null || !target.getFigure().isVisible()) {
            editor.getOutlineViewer().select(element);
            return;
        }
        getGraphicalViewer().reveal(target);
        getGraphicalViewer().select(target);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void dispose() {
        getCommandStack().removeCommandStackEventListener(this);
        super.dispose();
    }

    private class EditorContextMenuProvider extends ContextMenuProvider {
        public EditorContextMenuProvider(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void buildContextMenu(IMenuManager menu) {
            GEFActionConstants.addStandardActionGroups(menu);
            IAction action;
            action = getActionRegistry().getAction(ActionFactory.COPY.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.PASTE.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.REDO.getId());
            menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
            action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
            }
            List<EditPart> editParts = getGraphicalViewer().getSelectedEditParts();
            GraphElement graphElement = null;
            if (editParts.size() == 1) {
                graphElement = (GraphElement) editParts.get(0).getModel();
            } else if (editParts.size() == 0) {
                graphElement = editor.getDefinition();
            }
            ISelectionProvider selectionProvider = new StructuredSelectionProvider(graphElement);
            ObjectActionContributorManager.getManager().contributeObjectActions(editor, menu, selectionProvider);
//             ObjectActionContributorManager.getManager().contributeObjectActions(editor,
//             menu, selectionProvider, new
//             HashSet<IObjectActionContributor>());
        }
    }

    @Override
    protected FlyoutPreferences getPalettePreferences() {
        return new PaletteFlyoutPreferences();
    }

    @Override
    public void stackChanged(CommandStackEvent event) {
        if (Display.getCurrent() != null) {
            switch (event.getDetail()) {
            case CommandStack.POST_UNDO:
                editor.refresh();
            }
        }
    }
}

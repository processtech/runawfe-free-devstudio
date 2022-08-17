package ru.runa.gpd.ui.view;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.UiUtil;
import ru.runa.gpd.util.WorkspaceOperations;

public class SearchResultView extends ViewPart implements IPartListener {
    
    public static final String ID = "ru.runa.gpd.searchResultView";
    
    private TreeViewer viewer;
    private GraphitiProcessEditor previousEditor;

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
        viewer = new TreeViewer(parent, SWT.NONE);
        viewer.setContentProvider(new SearchResultContentProvider());
        viewer.setLabelProvider(new SearchResultLabelProvider());
        viewer.setInput(new Object());
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                try {
                    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.refresh();
                            }
                        }
                    });
                } catch (Exception e) {
                    // disposed
                }
            }
        });
        viewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (element instanceof SearchResultItem) {
                    List<GraphElement> path = SearchResultContentProvider.buildPath(((SearchResultItem)element).getGraphElement());
                    
                    GraphElement closestProcDef = path.stream().filter( si -> (si instanceof ProcessDefinition) ).findAny().orElse(null);
                    
                    String id = closestProcDef.getId();
                    IFile definitionFile = IOUtils.getFile(((id == null)?"":(id + ".")) + ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                    ProcessEditorBase editorPart = WorkspaceOperations.openProcessDefinition(definitionFile);
                    if (editorPart != null) {
                        editorPart.setFocus();
                        editorPart.select(((SearchResultItem) element).getGraphElement());
                    }
                   
                }
            }
        });
                       
    }
    
    public void refreshContent() {
        viewer.refresh();
        viewer.expandAll(); 
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof GraphitiProcessEditor) {
            if (previousEditor != null && (GraphitiProcessEditor) part != previousEditor
                    && processesNotBound((GraphitiProcessEditor) part, previousEditor)) {
                SearchResultContentProvider.clearResults();
                refreshContent();
            }
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
        IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        if (editors.length == 0) {
            SearchResultContentProvider.clearResults();
            refreshContent();
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
        if (part instanceof GraphitiProcessEditor) {
            previousEditor = (GraphitiProcessEditor) part;
        }
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
    }

    private boolean processesNotBound(GraphitiProcessEditor firstProcessEditor, GraphitiProcessEditor secondProcessEditor) {
        if (secondProcessEditor == null) {
            return true;
        }
        ProcessDefinition firstDefinition = firstProcessEditor.getDefinition();
        ProcessDefinition secondDefinition = secondProcessEditor.getDefinition();
        GraphElement parentElement = getRootParent(firstDefinition);
        ProcessDefinition parentDefinition = parentElement == null ? firstDefinition : parentElement.getProcessDefinition();
        if (parentDefinition.getEmbeddedSubprocesses().containsValue(secondDefinition)
                || parentDefinition.getName().equals(secondDefinition.getName())) {
            return false;
        }
        return true;
    }

    private GraphElement getRootParent(GraphElement processDefinition) {
        GraphElement parent = processDefinition.getParent();
        if (parent != null) {
            getRootParent(parent);
        }
        return parent;
    }
}

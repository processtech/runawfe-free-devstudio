package ru.runa.gpd.ui.view;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.runa.gpd.DataSourcesNature;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.wfe.InternalApplicationException;

public class DataSourceExplorerTreeView extends ViewPart implements ISelectionListener {
    private TreeViewer viewer;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        
        try {
            IProject dsProject = IOUtils.getDataSourcesProject();
            if (!dsProject.exists()) {
                IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(dsProject.getName());
                description.setNatureIds(new String[] { DataSourcesNature.NATURE_ID });
                dsProject.create(description, null);
                dsProject.open(IResource.BACKGROUND_REFRESH, null);
                dsProject.refreshLocal(IResource.DEPTH_ONE, null);
            }
        } catch (CoreException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.NONE);
        viewer.setContentProvider(new DataSourceTreeContentProvider());
        viewer.setLabelProvider(new DataSourceResourcesLabelProvider());
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
                if (element instanceof IFile) {
                    WorkspaceOperations.editDataSource((IStructuredSelection) viewer.getSelection());
                }
            }
        });
        getSite().setSelectionProvider(viewer);
        MenuManager menuMgr = new MenuManager();
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                DataSourceExplorerTreeView.this.fillContextMenu(manager);
            }
        });
        viewer.getControl().setMenu(menu);
    }

    @SuppressWarnings("unchecked")
    protected void fillContextMenu(IMenuManager manager) {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        final Object selectedObject = selection.getFirstElement();
        final List<IResource> resources = selection.toList();
        final boolean dsSelected = selectedObject instanceof IFile;
        manager.add(new Action(Localization.getString("DSExplorerTreeView.menu.label.addDS"), SharedImages.getImageDescriptor("icons/add_obj.gif")) {
            @Override
            public void run() {
                WorkspaceOperations.addDataSource();
            }
        });
        manager.add(new Action(Localization.getString("DSExplorerTreeView.menu.label.importDS"), SharedImages.getImageDescriptor("icons/import_ds.gif")) {
            @Override
            public void run() {
                WorkspaceOperations.importDataSource();
            }
        });
        if (dsSelected) {
            manager.add(new Action(Localization.getString("DSExplorerTreeView.menu.label.editDS"), SharedImages.getImageDescriptor("icons/rename.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.editDataSource(selection);
                }
            });
            manager.add(new Action(Localization.getString("DSExplorerTreeView.menu.label.copyDS"), SharedImages.getImageDescriptor("icons/copy.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.copyDataSource(selection);
                }
            });
            manager.add(new Action(Localization.getString("DSExplorerTreeView.menu.label.exportDS"), SharedImages.getImageDescriptor("icons/export_ds.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.exportDataSource(selection);
                }
            });
        }
        if (!selection.isEmpty()) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.refresh"), SharedImages.getImageDescriptor("icons/refresh.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.refreshResources(resources);
                }
            });
            manager.add(new Action(Localization.getString("button.delete"), SharedImages.getImageDescriptor("icons/delete.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.deleteDataSources(resources);
                }
            });
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    }
}

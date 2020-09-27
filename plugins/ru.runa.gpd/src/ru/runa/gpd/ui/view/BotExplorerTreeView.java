package ru.runa.gpd.ui.view;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.NewSearchUI;
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
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.search.BaseSearchQuery;
import ru.runa.gpd.search.BotSearchQuery;
import ru.runa.gpd.search.BotTaskSearchQuery;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.ui.wizard.ExportBotStationWizardPage;
import ru.runa.gpd.ui.wizard.ExportBotTaskWizardPage;
import ru.runa.gpd.ui.wizard.ExportBotWizardPage;
import ru.runa.gpd.ui.wizard.ImportBotStationWizardPage;
import ru.runa.gpd.ui.wizard.ImportBotTaskWizardPage;
import ru.runa.gpd.ui.wizard.ImportBotWizardPage;
import ru.runa.gpd.util.UiUtil;
import ru.runa.gpd.util.WorkspaceOperations;

public class BotExplorerTreeView extends ViewPart implements ISelectionListener {
    private TreeViewer viewer;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        UiUtil.hideToolBar(getViewSite());
        viewer = new TreeViewer(parent, SWT.NONE);
        viewer.setContentProvider(new BotTreeContentProvider());
        viewer.setLabelProvider(new BotResourcesLabelProvider());
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
                    WorkspaceOperations.openBotTask((IFile) element);
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
                BotExplorerTreeView.this.fillContextMenu(manager);
            }
        });
        viewer.getControl().setMenu(menu);
    }

    @SuppressWarnings("unchecked")
    protected void fillContextMenu(IMenuManager manager) {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        final Object selectedObject = selection.getFirstElement();
        final List<IResource> resources = selection.toList();
        final boolean menuOnBotStation = selectedObject instanceof IProject;
        final boolean menuOnBot = selectedObject instanceof IFolder;
        final boolean menuOnBotTask = selectedObject instanceof IFile;
        manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.newBotStation"),
                SharedImages.getImageDescriptor("icons/add_bot_station.gif")) {
            @Override
            public void run() {
                WorkspaceOperations.createNewBotStation(selection);
            }
        });
        manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.importBotStation"),
                SharedImages.getImageDescriptor("icons/import_bot.gif")) {
            @Override
            public void run() {
                WorkspaceOperations.importBotElement(selection, new ImportBotStationWizardPage(selection));
            }
        });
        if (menuOnBotStation) {
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.exportBotStation"),
                    SharedImages.getImageDescriptor("icons/export_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.exportBotElement(selection, new ExportBotStationWizardPage(selection));
                }
            });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.newBot"),
                    SharedImages.getImageDescriptor("icons/add_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.createNewBot(selection);
                }
            });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.importBot"),
                    SharedImages.getImageDescriptor("icons/import_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.importBotElement(selection, new ImportBotWizardPage(selection));
                }
            });
            manager.add(new Action(Localization.getString("RenameBotStationDialog.edit"), SharedImages.getImageDescriptor("icons/rename.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.renameBotStationFolder(selection);
                }
            });
        }
        if (menuOnBot) {
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.newBot"),
                    SharedImages.getImageDescriptor("icons/add_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.createNewBot(selection);
                }
            });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.exportBot"),
                    SharedImages.getImageDescriptor("icons/export_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.exportBotElement(selection, new ExportBotWizardPage(selection));
                }
            });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.newBotTask"),
                    SharedImages.getImageDescriptor("icons/bot_task.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.createNewBotTask(selection);
                }
            });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.importBotTask"),
                    SharedImages.getImageDescriptor("icons/import_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.importBotElement(selection, new ImportBotTaskWizardPage(selection));
                }
            });
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.renameProcess"),
                    SharedImages.getImageDescriptor("icons/rename.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.renameBotFolder(selection);
                }
            });
        }
        if (menuOnBotTask) {
            manager.add(new Action(Localization.getString("ExplorerTreeView.menu.label.renameProcess"),
                    SharedImages.getImageDescriptor("icons/rename.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.renameBotTaskFile(selection);
                }
            });
            manager.add(
                    new Action(Localization.getString("ExplorerTreeView.menu.label.copyProcess"), SharedImages.getImageDescriptor("icons/copy.gif")) {
                        @Override
                        public void run() {
                            WorkspaceOperations.copyBotTask(selection);
                        }
                    });
            manager.add(new Action(Localization.getString("BotExplorerTreeView.menu.label.exportBotTask"),
                    SharedImages.getImageDescriptor("icons/export_bot.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.exportBotElement(selection, new ExportBotTaskWizardPage(selection));
                }
            });
        }
        if (!selection.isEmpty()) {
            manager.add(
                    new Action(Localization.getString("ExplorerTreeView.menu.label.refresh"), SharedImages.getImageDescriptor("icons/refresh.gif")) {
                        @Override
                        public void run() {
                            WorkspaceOperations.refreshResources(resources);
                        }
                    });
            manager.add(new Action(Localization.getString("button.delete"), SharedImages.getImageDescriptor("icons/delete.gif")) {
                @Override
                public void run() {
                    WorkspaceOperations.deleteBotResources(resources);
                }
            });
        }
        if (menuOnBot || menuOnBotTask) {
            manager.add(new Action(Localization.getString("button.findReferences"), SharedImages.getImageDescriptor("icons/search.gif")) {
                @Override
                public void run() {
                    try {
                        IResource resource = (IResource) selectedObject;
                        BaseSearchQuery query;
                        if (menuOnBot) {
                            query = new BotSearchQuery(resource.getName());
                        } else {
                            query = new BotTaskSearchQuery(resource.getParent().getName(), resource.getName());
                        }
                        NewSearchUI.runQueryInBackground(query);
                    } catch (Exception ex) {
                        PluginLogger.logError(ex);
                    }
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

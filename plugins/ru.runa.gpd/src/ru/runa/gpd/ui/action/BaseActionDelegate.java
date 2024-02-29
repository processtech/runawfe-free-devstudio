package ru.runa.gpd.ui.action;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.DataSourcesNature;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.editor.ProcessEditorBase;

public abstract class BaseActionDelegate implements IWorkbenchWindowActionDelegate {
    protected IWorkbenchWindow window;

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    protected IEditorPart getActiveEditor() {
        return window.getActivePage().getActiveEditor();
    }

    protected List<ProcessEditorBase> getOpenedDesignerEditors() {
        List<ProcessEditorBase> editors = new ArrayList<ProcessEditorBase>();
        IEditorReference[] editorReferences = window.getActivePage().getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
            IEditorPart editor = editorReference.getEditor(true);
            if (editor instanceof ProcessEditorBase) {
                editors.add((ProcessEditorBase) editor);
            }
        }
        return editors;
    }

    protected ProcessEditorBase getActiveDesignerEditor() {
        IEditorPart editor = getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return (ProcessEditorBase) editor;
        }
        return null;
    }

    protected IStructuredSelection getStructuredSelection() {
        ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        IEditorPart editorPart = getActiveEditor();
        if (editorPart != null) {
            IEditorInput editorInput = editorPart.getEditorInput();
            if (editorInput instanceof FileEditorInput) {
                return new StructuredSelection(((FileEditorInput) editorInput).getFile());
            }
        }
        return null;
    }

    protected boolean isBotStructuredSelection() {
        IResource resource = getSelectedResource();
        if (resource != null) {
            try {
                return resource.exists() && resource.getProject().getNature(BotStationNature.NATURE_ID) != null;
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
        return getActiveEditor() instanceof BotTaskEditor;
    }

    protected boolean isDataSourceStructuredSelection() {
        IResource resource = getSelectedResource();
        if (resource != null) {
            try {
                return resource.exists() && resource.getProject().getNature(DataSourcesNature.NATURE_ID) != null;
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
        return false;
    }

    protected boolean isResource() {
        return getSelectedResource() != null;
    }

    private IResource getSelectedResource() {
        IStructuredSelection selection = getStructuredSelection();
        if (selection != null) {
            Object selectedObject = selection.getFirstElement();
            if (selectedObject instanceof IResource) {
                return (IResource) selectedObject;
            }
        }
        return null;
    }
}

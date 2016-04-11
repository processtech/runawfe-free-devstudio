package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.OutlineViewer;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;

public abstract class BaseModelActionDelegate implements IObjectActionDelegate {
    private IWorkbenchPart workbenchPart;
    private GraphElement selectedElement;

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.workbenchPart = targetPart;
    }

    public void initModelActionDelegate(BaseModelActionDelegate actionDelegate) {
        actionDelegate.workbenchPart = workbenchPart;
        actionDelegate.selectedElement = selectedElement;
    }

    protected <T extends GraphElement> T getSelection() {
        return (T) selectedElement;
    }

    protected <T extends GraphElement> T getSelectionNotNull() {
        if (selectedElement == null) {
            throw new RuntimeException("selection is null");
        }
        return (T) selectedElement;
    }

    protected IWorkbenchPage getWorkbenchPage() {
        return getActiveEditor().getEditorSite().getPage();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        selectedElement = null;
        if (selection instanceof StructuredSelection) {
            Object object = ((StructuredSelection) selection).getFirstElement();
            if (object instanceof GraphElement) {
                selectedElement = (GraphElement) object;
            }
        }
    }

    protected void executeCommand(Command command) {
        CommandStack commandStack = null;
        if (workbenchPart instanceof ProcessEditorBase) {
            commandStack = ((ProcessEditorBase) workbenchPart).getCommandStack();
        } else if (workbenchPart instanceof GraphicalEditor) {
            commandStack = (CommandStack) ((GraphicalEditor) workbenchPart).getAdapter(CommandStack.class);
        } else if (workbenchPart instanceof ContentOutline) {
            commandStack = ((OutlineViewer) ((ContentOutline) workbenchPart).getCurrentPage()).getCommandStack();
            command.execute();
        } else {
            PluginLogger.logInfo("Unapplicable targetPart: " + workbenchPart);
        }
        if (commandStack != null) {
            commandStack.execute(command);
        }
    }

    protected IEditorPart getActiveEditor() {
        return workbenchPart.getSite().getPage().getActiveEditor();
    }

    protected ProcessEditorBase getActiveDesignerEditor() {
        IEditorPart editor = getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return (ProcessEditorBase) editor;
        }
        return null;
    }

    protected IFile getDefinitionFile() {
        return getActiveDesignerEditor().getDefinitionFile();
    }

}

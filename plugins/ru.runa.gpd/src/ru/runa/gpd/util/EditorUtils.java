package ru.runa.gpd.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;

public class EditorUtils {
    public static synchronized void closeEditorIfRequired(IResourceChangeEvent event, final IFile file, final IEditorPart editor) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            IResourceDelta delta = event.getDelta().findMember(file.getFullPath());
            if (delta != null) {
                IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta) {
                        if (delta.getKind() == IResourceDelta.REMOVED) {
                            if (file.equals(delta.getResource())) {
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            editor.getSite().getPage().closeEditor(editor, false);
                                        } catch (Exception e) {
                                            PluginLogger.logErrorWithoutDialog("Close editor on delete", e);
                                        }
                                    }
                                });
                                return false;
                            }
                        }
                        return true;
                    }
                };
                try {
                    delta.accept(visitor);
                } catch (CoreException e) {
                    PluginLogger.logErrorWithoutDialog("Visit on delete", e);
                }
            }
        }
    }

    public static ProcessEditorBase getOpenedEditor(IFile definitionFile) throws PartInitException {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow == null) {
            return null;
        }
        for (IEditorReference editorRef : activeWindow.getActivePage().getEditorReferences()) {
            if (editorRef.getEditorInput() instanceof IFileEditorInput) {
                IFileEditorInput editorInput = (IFileEditorInput) editorRef.getEditorInput();
                if (definitionFile.equals(editorInput.getFile())) {
                    return (ProcessEditorBase) editorRef.getEditor(true);
                }
            }
        }
        return null;
    }

    public static ProcessEditorBase getCurrentEditor() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow == null) {
            return null;
        }
        IEditorPart editorPart = activeWindow.getActivePage().getActiveEditor();
        if (editorPart instanceof ProcessEditorBase) {
            return (ProcessEditorBase) editorPart;
        }
        return null;
    }
}

package ru.runa.gpd.util;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.par.ParContentProvider;

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
                                editor.getSite().getPage().closeEditor(editor, false);
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

    public static IViewPart showView(String viewId) {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
        } catch (PartInitException e) {
            PluginLogger.logErrorWithoutDialog(viewId, e);
            return null;
        }
    }

    public static void hideView(String viewId) {
        IViewReference[] viewParts = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
        for (IViewReference viewReference : viewParts) {
            if (Objects.equal(viewReference.getId(), viewId)) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewReference);
                return;
            }
        }
    }

    public static ProcessEditorBase openEditorByElement(GraphElement graphElement) {
        Optional<GraphElement> closestProcessDefinitionOptional = Optional.ofNullable(graphElement.getProcessDefinition());
        if (closestProcessDefinitionOptional.isPresent()) {
            String id = closestProcessDefinitionOptional.get().getId();
            if (id != null) {
                IFile definitionFile = IOUtils.getFile(((id == null) ? "" : (id + ".")) + ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                return WorkspaceOperations.openProcessDefinition(definitionFile);
            }
        }
        return getCurrentEditor();
    }

    private static boolean defineParentProcessDefinitions(ProcessDefinition targetProcessDefinition, ProcessDefinition parentProcessDefinition,
            List<ProcessDefinition> processDefinitions) {
        if (parentProcessDefinition != null) {
            List<Subprocess> subprocessNodes = parentProcessDefinition.getChildrenRecursive(Subprocess.class);
            if (subprocessNodes.isEmpty()) {
                return false;
            }
            for (Subprocess subprocess : subprocessNodes) {
                if (subprocess.isEmbedded() && subprocess.getEmbeddedSubprocess() != null
                        && subprocess.getEmbeddedSubprocess().equals(targetProcessDefinition)) {
                    return true;
                }
                if (defineParentProcessDefinitions(targetProcessDefinition, subprocess.getEmbeddedSubprocess(), processDefinitions)) {
                    processDefinitions.add(0, subprocess.getEmbeddedSubprocess());
                    return true;
                }
            }
        }
        return false;
    }

    public static void updateOutlineOfParentProcesses(ProcessDefinition processDefinition, IWorkbenchPage page) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IViewReference[] openViews = activeWindow.getActivePage().getViewReferences();
        if (!Arrays.stream(openViews).map(viewRef -> viewRef.getView(false)).anyMatch(view -> view instanceof ContentOutline)) {
            return;
        }
        List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        processDefinitions.add(processDefinition.getMainProcessDefinition());
        defineParentProcessDefinitions(processDefinition, processDefinition.getMainProcessDefinition(), processDefinitions);
        List<IFile> definitionsFiles = processDefinitions.stream().map(definition -> definition.getFile()).collect(Collectors.toList());
        if (activeWindow != null) {
            for (IEditorReference editorRef : activeWindow.getActivePage().getEditorReferences()) {
                IEditorPart editorPart = editorRef.getEditor(true);
                if (editorPart instanceof ProcessEditorBase && definitionsFiles.contains(((FileEditorInput) editorPart.getEditorInput()).getFile())) {
                    ((ProcessEditorBase) editorPart).getOutlineViewer().refreshTreeView();
                }
            }
        }
    }
}

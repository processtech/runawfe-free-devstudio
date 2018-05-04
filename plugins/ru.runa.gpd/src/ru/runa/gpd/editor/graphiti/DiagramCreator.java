package ru.runa.gpd.editor.graphiti;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.PluginLogger;

public class DiagramCreator {
    private final static String TEMPFILE_EXTENSION = "bpmn2d";
    private IFolder diagramFolder;
    private IFile diagramFile;
    private URI uri;

    public DiagramCreator(IFile definitionFile) {
        IPath fullPath = definitionFile.getFullPath();
        IFolder folder = getTempFolder(fullPath);
        diagramFile = getTempFile(fullPath, folder);
    }

    public DiagramEditorInput createDiagram(String contentFileName) {
        try {
            if (diagramFolder != null && !diagramFolder.exists()) {
                diagramFolder.create(false, true, null);
            }
            Diagram diagram = Graphiti.getPeCreateService().createDiagram("BPMNdiagram", diagramFile.getFullPath().removeFileExtension().lastSegment(), true);
            // permanently hide grid on diagram
            //diagram.setGridUnit(0);
            //diagram.setVerticalGridUnit(0);
            uri = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
            if (contentFileName != null) {
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(contentFileName);
                IFile modelFile = getModelFile(new Path(uri.trimFragment().toPlatformString(true)));
                String filePath = modelFile.getLocationURI().getRawPath();
                filePath = filePath.replaceAll("%20", " ");
                try {
                    OutputStream out = new FileOutputStream(filePath);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (Exception e) {
                    PluginLogger.logError(e);
                }
            }
            createEmfFileForDiagram(uri, diagram);
            String providerId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
            DiagramEditorInput editorInput = new DiagramEditorInput(EcoreUtil.getURI(diagram), providerId);
            return editorInput;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public IFolder getDiagramFolder() {
        return diagramFolder;
    }

    public IFile getDiagramFile() {
        return diagramFile;
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Construct a temporary folder based on the given path. The folder is
     * constructed in the project root and its name will be the same as the given
     * path's file extension.
     * 
     * @param fullPath
     *          - path of the actual BPMN2 model file
     * @return an IFolder for the temporary folder.
     * @throws CoreException
     */
    public static IFolder getTempFolder(IPath fullPath) {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            String name = fullPath.getFileExtension();
            if (name == null || name.length() == 0) {
                name = "bpmn";
            }
            String dir = fullPath.segment(0);
            IFolder folder = root.getProject(dir).getFolder("." + name);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            String[] segments = fullPath.segments();
            for (int i = 1; i < segments.length - 1; i++) {
                String segment = segments[i];
                folder = folder.getFolder(segment);
                if (!folder.exists()) {
                    folder.create(true, true, null);
                }
            }
            return folder;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the temporary file to be used as editor input. Conceptually, this is
     * the "diagramFile" mentioned here which is just a placeholder for use by
     * Graphiti as the DiagramEditorInput file.
     * 
     * @param fullPath
     *          - path of the actual BPMN2 model file
     * @param folder
     *          - folder containing the model file
     * @return an IFile for the temporary file. If the file exists, it is first
     *         deleted.
     */
    public static IFile getTempFile(IPath fullPath, IFolder folder) {
        IPath path = fullPath.removeFileExtension().addFileExtension(TEMPFILE_EXTENSION);
        IFile tempFile = folder.getFile(path.lastSegment());
        // We don't need anything from that file and to be sure there are no side
        // effects we delete the file
        if (tempFile.exists()) {
            try {
                tempFile.delete(true, null);
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
        return tempFile;
    }

    /**
     * Return the BPMN2 model file given a path to either the "diagramFile"
     * temporary file, or the actual model file.
     * 
     * @param fullPath
     *          - path of the actual BPMN2 model file
     * @return an IFile for the model file.
     */
    public static IFile getModelFile(IPath fullPath) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath).getProject();
        int matchingSegments = project.getFullPath().matchingFirstSegments(fullPath);
        int totalSegments = fullPath.segmentCount();
        String ext = fullPath.getFileExtension();
        // sanity check: make sure the fullPath is not the project
        if (totalSegments <= matchingSegments) {
            return null;
        }
        String[] segments = fullPath.segments();
        IPath path = null;
        if (TEMPFILE_EXTENSION.equals(ext)) {
            // this is a tempFile - rebuild the BPMN2 model file name from its path
            ext = fullPath.segment(matchingSegments);
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            path = project.getFullPath();
            for (int i = matchingSegments + 1; i < segments.length; ++i) {
                path = path.append(segments[i]);
            }
            path = path.removeFileExtension().addFileExtension(ext);
        } else {
            // this is a model file - normalize path
            path = fullPath.makeAbsolute();
        }
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }

    public static TransactionalEditingDomain createEmfFileForDiagram(URI diagramResourceUri, final Diagram diagram) {
        // Create a resource set and EditingDomain
        final TransactionalEditingDomain editingDomain = GraphitiUiInternal.getEmfService().createResourceSetAndEditingDomain();
        final ResourceSet resourceSet = editingDomain.getResourceSet();
        // Create a resource for this file.
        final Resource resource = resourceSet.createResource(diagramResourceUri);
        final CommandStack commandStack = editingDomain.getCommandStack();
        commandStack.execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {
                resource.setTrackingModification(true);
                resource.getContents().add(diagram);
            }
        });
        save(editingDomain, Collections.<Resource, Map<?, ?>> emptyMap());
        return editingDomain;
    }

    private static void save(TransactionalEditingDomain editingDomain, Map<Resource, Map<?, ?>> options) {
        saveInWorkspaceRunnable(editingDomain, options);
    }

    private static void saveInWorkspaceRunnable(final TransactionalEditingDomain editingDomain, final Map<Resource, Map<?, ?>> options) {
        final Map<URI, Throwable> failedSaves = new HashMap<URI, Throwable>();
        final IWorkspaceRunnable wsRunnable = new IWorkspaceRunnable() {
            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Transaction parentTx;
                        if (editingDomain != null && (parentTx = ((TransactionalEditingDomainImpl) editingDomain).getActiveTransaction()) != null) {
                            do {
                                if (!parentTx.isReadOnly()) {
                                    throw new IllegalStateException("FileService.save() called from within a command (likely produces a deadlock)"); //$NON-NLS-1$
                                }
                            } while ((parentTx = ((TransactionalEditingDomainImpl) editingDomain).getActiveTransaction().getParent()) != null);
                        }
                        final EList<Resource> resources = editingDomain.getResourceSet().getResources();
                        // Copy list to an array to prevent
                        // ConcurrentModificationExceptions
                        // during the saving of the dirty resources
                        Resource[] resourcesArray = new Resource[resources.size()];
                        resourcesArray = resources.toArray(resourcesArray);
                        final Set<Resource> savedResources = new HashSet<Resource>();
                        for (int i = 0; i < resourcesArray.length; i++) {
                            // In case resource modification tracking is
                            // switched on, we can check if a resource
                            // has been modified, so that we only need to same
                            // really changed resources; otherwise
                            // we need to save all resources in the set
                            final Resource resource = resourcesArray[i];
                            if (resource.isModified()) {
                                try {
                                    resource.save(options.get(resource));
                                    savedResources.add(resource);
                                } catch (final Throwable t) {
                                    failedSaves.put(resource.getURI(), t);
                                }
                            }
                        }
                    }
                };
                try {
                    editingDomain.runExclusive(runnable);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
                editingDomain.getCommandStack().flush();
            }
        };
        try {
            ResourcesPlugin.getWorkspace().run(wsRunnable, null);
            if (!failedSaves.isEmpty()) {
                throw new WrappedException(createMessage(failedSaves), new RuntimeException());
            }
        } catch (final CoreException e) {
            final Throwable cause = e.getStatus().getException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(e);
        }
    }

    private static String createMessage(Map<URI, Throwable> failedSaves) {
        final StringBuilder buf = new StringBuilder("The following resources could not be saved:");
        for (final Entry<URI, Throwable> entry : failedSaves.entrySet()) {
            buf.append("\nURI: ").append(entry.getKey().toString()).append(", cause: \n").append(getExceptionAsString(entry.getValue()));
        }
        return buf.toString();
    }

    private static String getExceptionAsString(Throwable t) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        final String result = stringWriter.toString();
        try {
            stringWriter.close();
        } catch (final IOException e) {
            // $JL-EXC$ ignore
        }
        printWriter.close();
        return result;
    }

    public void disposeDiagram() {
        if (diagramFile.exists()) {
            try {
                diagramFile.getParent().delete(true, null);
                diagramFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
    }

}

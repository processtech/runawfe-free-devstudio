/*
 * Created on 24.09.2005
 */
package ru.runa.gpd.ui.view;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

import com.google.common.collect.Lists;

public class ProcessExplorerContentProvider implements ITreeContentProvider {
    
    @Override
    public Object[] getChildren(Object parentElement) {
        List<IResource> resources = Lists.newArrayList();
        if (parentElement instanceof IProject) {
            IProject project = (IProject) parentElement;
            if (IOUtils.isProjectHasProcessNature(project)) {
                findFolders(project, resources);
            } else {
                List<IFile> files = IOUtils.getProcessDefinitionFiles(project);
                for (IFile file : files) {
                    resources.add((IFolder) file.getParent());
                }
            }
        }
        if (parentElement instanceof IFolder) {
            IFolder folder = (IFolder) parentElement;
            if (IOUtils.isProcessDefinitionFolder(folder)) {
                findSubProcessFiles(folder, resources);
            } else {
                findFolders(folder, resources);
            }
        }
        return resources.toArray(new IResource[resources.size()]);
    }

    private static void findFolders(IContainer container, List<IResource> result) {
        try {
            for (IResource resource : container.members()) {
                if (resource instanceof IFolder) {
                    IFolder folder = (IFolder) resource;
                    if (IOUtils.isProcessDefinitionFolder(folder)) {
                        result.add(folder);
                        continue;
                    }
                    if (folder.getName().startsWith(".")) {
                        continue;
                    }
                    result.add(folder);
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    private static void findSubProcessFiles(IContainer container, List<IResource> result) {
        try {
            for (IResource resource : container.members()) {
                if (resource.getName().endsWith(ParContentProvider.PROCESS_DEFINITION_FILE_NAME) &&
                        !resource.getName().equals(ParContentProvider.PROCESS_DEFINITION_FILE_NAME)) {
                    result.add(resource);
                }
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object parentElement) {
        return getChildren(parentElement).length > 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return IOUtils.getAllProcessDefinitionProjects();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}

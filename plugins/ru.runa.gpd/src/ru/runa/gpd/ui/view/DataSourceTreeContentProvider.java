package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.runa.gpd.DataSourcesNature;
import ru.runa.gpd.util.IOUtils;

public class DataSourceTreeContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getChildren(Object element) {
        /*
        if (parentElement instanceof IProject) {
            // List<IFolder> botFolders = IOUtils.getBotFolders((IProject) parentElement);
            // return botFolders.toArray();
            try {
                return ((IProject) parentElement).members();
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (parentElement instanceof IFolder) {
            List<IFile> files = IOUtils.getBotTaskFiles((IFolder) parentElement);
            return files.toArray();
        }
        */
        /*
        if (element instanceof IProject) {
            try {
                List<Object> list = new ArrayList<Object>();
                for (IResource dsResource : ((IProject) element).members()) {
                    if (dsResource instanceof IFile && ((IFile) dsResource).getFileExtension().equalsIgnoreCase("xml")) {
                        list.add(dsResource);
                    }
                }
                return list.toArray();
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        /*
        if (element instanceof IProject) {
            try {
                return ((IProject) element).members().length > 0;
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<Object> returnList = new ArrayList<Object>();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            for (IResource resource : workspace.getRoot().members()) {
                if (resource instanceof IProject && ((IProject) resource).getNature(DataSourcesNature.NATURE_ID) != null) {
                    for (IResource dsResource : ((IProject) resource).members()) {
                        if (dsResource instanceof IFile && ((IFile) dsResource).getFileExtension().equalsIgnoreCase("xml")) {
                            returnList.add(dsResource);
                        }
                    }
                }
            }
            return returnList.toArray();
        } catch (CoreException e) {
            return new Object[] {};
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}

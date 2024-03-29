package ru.runa.gpd.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.IOUtils;

public class ProcessExplorerLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof IFile) {
            ProcessDefinition definition = ProcessCache.getProcessDefinition((IFile) element);
            if (definition != null) {
                return definition.getName();
            }
            return ((IFile) element).getName();
        }
        if (element instanceof IResource) {
            if (element instanceof IFolder && GlobalSectionUtils.isGlobalSectionFolder((IFolder) element)) {
                return GlobalSectionUtils.getLabel((IResource) element);
            }
            return ((IResource) element).getName();
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IProject) {
            return SharedImages.getImage("icons/project.gif");
        }
        if (element instanceof IFolder) {
            IFolder folder = (IFolder) element;
            if (IOUtils.isProcessDefinitionFolder(folder)) {
                return SharedImages.getImage("icons/process.gif");
            }
            if (GlobalSectionUtils.isGlobalSectionFolder(folder)) {
                return SharedImages.getImage("icons/glb.gif");
            }
            return SharedImages.getImage("icons/folder.gif");
        }
        if (element instanceof IFile) {
            // embedded subprocess
            return SharedImages.getImage("icons/process.gif");
        }
        throw new IllegalArgumentException("Unexpected " + element);
    }
}

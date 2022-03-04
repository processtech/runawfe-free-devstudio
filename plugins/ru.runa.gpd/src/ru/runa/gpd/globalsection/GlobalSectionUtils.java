package ru.runa.gpd.globalsection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import ru.runa.gpd.util.IOUtils;

public class GlobalSectionUtils {
    private GlobalSectionUtils() {
    }

    public static final String GLOBAL_SECTION_PREFIX = ".";

    public static boolean isGlobalSectionName(String name) {
        return name == null ? false : name.startsWith(GLOBAL_SECTION_PREFIX);
    }

    public static boolean isGlobalSectionFolder(IFolder folder) {
        return folder == null ? false : isGlobalSectionName(folder.getName()) && IOUtils.getProcessDefinitionFile(folder).exists();
    }

    public static boolean isGlobalSectionContainer(IContainer container) {
        return container == null ? false : isGlobalSectionName(container.getName());
    }

    public static boolean isGlobalSectionResource(IResource resource) {
        return resource == null ? false : isGlobalSectionName(resource.getName());
    }

    public static String getLabel(String name) {
        return name.substring(1);
    }

    public static String getLabel(IResource resource) {
        return getLabel(resource.getName());
    }
}

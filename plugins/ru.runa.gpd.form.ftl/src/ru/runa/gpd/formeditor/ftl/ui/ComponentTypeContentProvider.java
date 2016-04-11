package ru.runa.gpd.formeditor.ftl.ui;

import java.util.List;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;

import com.google.common.collect.Lists;

public enum ComponentTypeContentProvider {
    INSTANCE;

    public List<ComponentType> getModel() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (activePage != null && activePage.getActiveEditor() instanceof IComponentDropTarget) {
                return ComponentTypeRegistry.getEnabled();
            }
        }
        return Lists.newArrayList();
    }
}
package ru.runa.gpd;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Until 4.1.0 there was java project with predefined src/process folder.
 * @since 4.1.0
 */
public class ProcessProjectNature implements IProjectNature {
    public static final String NATURE_ID = "ru.runa.gpd.processNature";
    private IProject project;

    @Override
    public void configure() throws CoreException {
    }

    @Override
    public void deconfigure() throws CoreException {
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }
}

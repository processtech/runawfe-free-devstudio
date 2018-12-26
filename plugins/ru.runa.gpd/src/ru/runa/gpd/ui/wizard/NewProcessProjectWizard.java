package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessProjectNature;

public class NewProcessProjectWizard extends Wizard implements INewWizard {
    private WizardNewProjectCreationPage mainPage;

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        setWindowTitle(Localization.getString("NewProcessProjectWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new NewProcessProjectWizardPage("basicNewProjectPage");
        mainPage.setTitle(Localization.getString("NewProcessProjectWizard.page.title"));
        mainPage.setDescription(Localization.getString("NewProcessProjectWizard.page.description"));
        this.addPage(mainPage);
    }

    private IProject createNewProject() throws Exception {
        final IProject newProject = mainPage.getProjectHandle();
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
        if (!mainPage.useDefaults()) {
            description.setLocation(mainPage.getLocationPath());
        }
        description.setNatureIds(new String[] { ProcessProjectNature.NATURE_ID });
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 3000);
                    newProject.create(description, new SubProgressMonitor(monitor, 1000));
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    newProject.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
                    newProject.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1000));
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
        return newProject;
    }

    @Override
    public boolean performFinish() {
        try {
            createNewProject();
        } catch (InvocationTargetException e) {
            PluginLogger.logError(Localization.getString("NewProcessProjectWizard.error.creation"), e.getTargetException());
            return false;
        } catch (Exception e) {
            PluginLogger.logError(Localization.getString("NewProcessProjectWizard.error.creation"), e);
            return false;
        }
        return true;
    }
}

package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.IOUtils;

public class NewBotStationWizard extends Wizard implements INewWizard {
    private NewBotStationWizardPage page;
    private IProject newBotStation;

    public NewBotStationWizard() {
        setWindowTitle(Localization.getString("NewBotStationWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new NewBotStationWizardPage(Localization.getString("NewBotStationWizardPage.page.name"));
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("NewBotStationWizard.monitor.title"), 3);
                        newBotStation = createNewBotStation();
                        monitor.worked(1);
                        IFolder folder = newBotStation.getProject().getFolder("/src/botstation/");
                        IOUtils.createFolder(folder);
                        monitor.worked(1);
                        IFile file = folder.getFile("botstation");
                        IOUtils.createOrUpdateFile(file, BotTaskUtils.createBotStationInfo(page.getProjectName(), page.getRmiAddress()));
                        monitor.worked(1);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            PluginLogger.logError(Localization.getString("NewBotStationWizard.error.creation"), e.getTargetException());
            return false;
        } catch (InterruptedException e) {
        }
        return true;
    }

    private IProject createNewBotStation() throws InvocationTargetException, InterruptedException {
        final IProject newProject = page.getProjectHandle();
        final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
        if (!page.useDefaults()) {
            description.setLocation(page.getLocationPath());
        }
        description.setNatureIds(new String[] { BotStationNature.NATURE_ID });
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 3000);
                    newProject.create(description, new SubProgressMonitor(monitor, 1000));
                    newProject.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
                    newProject.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1000));
                } finally {
                    monitor.done();
                }
            }
        };
        getContainer().run(true, true, op);
        return newProject;
    }
}

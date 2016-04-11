package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;

public class NewFolderWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;
    private NewFolderWizardPage page;

    public NewFolderWizard() {
        setWindowTitle(Localization.getString("NewFolderWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
    }

    @Override
    public void addPages() {
        page = new NewFolderWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("NewFolderWizard.monitor.title"), 4);
                        IFolder folder = page.getFolder();
                        folder.create(true, true, null);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            PluginLogger.logError(Localization.getString("NewFolderWizard.error.creation"), e.getTargetException());
            return false;
        } catch (InterruptedException e) {
        }
        return true;
    }

}

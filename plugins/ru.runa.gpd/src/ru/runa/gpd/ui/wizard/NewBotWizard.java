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

public class NewBotWizard extends Wizard implements INewWizard {
    private NewBotWizardPage page;
    private IStructuredSelection selection;

    @Override
    public void addPages() {
        super.addPages();
        page = new NewBotWizardPage(selection);
        addPage(page);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("NewProcessDefinitionWizard.monitor.title"), 4);
                        IFolder folder = page.getBotFolder();
                        folder.create(true, true, null);
                        monitor.worked(1);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            PluginLogger.logError(Localization.getString("NewProcessDefinitionWizard.error.creation"), e.getTargetException());
            return false;
        } catch (InterruptedException e) {
        }
        return true;
    }

}

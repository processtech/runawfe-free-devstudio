package ru.runa.gpd.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public class CopyProcessDefinitionWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;
    private CopyProcessDefinitionWizardPage page;

    public CopyProcessDefinitionWizard() {
        setWindowTitle(Localization.getString("CopyProcessDefinitionWizard.wizard.title"));
    }

    @Override
    public void init(IWorkbench w, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
    }

    @Override
    public void addPages() {
        IFolder sourceProcessFolder = (IFolder) selection.getFirstElement();
        page = new CopyProcessDefinitionWizardPage(sourceProcessFolder);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask(Localization.getString("CopyProcessDefinitionWizard.monitor.title"), 3);
                        monitor.worked(1);
                        IFolder targetFolder = page.getTargetProcessFolder();
                        page.getSourceProcessFolder().copy(targetFolder.getFullPath(), true, monitor);
                        IFile definitionFile = IOUtils.getProcessDefinitionFile(targetFolder);
                        monitor.worked(1);
                        ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                        boolean hasProcessNameBeenChanged = !Objects.equals(definition.getName(), page.getProcessName());
                        if (hasProcessNameBeenChanged) {
                        	definition.getVersionInfoList().clear();
                        	definition.setName(page.getProcessName());
                        }
                        definition.setLanguage(page.getLanguage());
                        WorkspaceOperations.saveProcessDefinition(definition);
                        ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                        WorkspaceOperations.refreshResource(targetFolder);
                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            PluginLogger.logError(e);
            return false;
        }
        return true;
    }
}

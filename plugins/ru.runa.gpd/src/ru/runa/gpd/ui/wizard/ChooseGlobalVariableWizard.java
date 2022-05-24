package ru.runa.gpd.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class ChooseGlobalVariableWizard extends Wizard {

    private ChooseGlobalVariableWizardPage mainPage;
    private IStructuredSelection selection;
    private Variable Variable;
    private List<IContainer> processContainers;
    private ProcessDefinition processDefinition;
    private IContainer initialSelection;
    private IFolder parentProcessDefinitionFolder;
    private ProcessDefinition parentProcessDefinition;

    public ChooseGlobalVariableWizard(ProcessDefinition processDefinition, IStructuredSelection selection) {
        this.processDefinition = processDefinition;
        this.selection = selection;
        IFile file = processDefinition.getFile();
        this.initialSelection = (IContainer) IOUtils.getProcessSelectionResource(selection);
        parentProcessDefinitionFolder = (IFolder) (file == null ? null : file.getParent());
        this.processContainers = IOUtils.getAllProcessContainers();
        this.initialSelection = (IContainer) parentProcessDefinitionFolder.getParent();
        mainPage = new ChooseGlobalVariableWizardPage(processDefinition, this.initialSelection);
    }

    @Override
    public void addPages() {
        addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        mainPage.finish();
        return true;
    }

}

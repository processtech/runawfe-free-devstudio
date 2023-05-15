package ru.runa.gpd.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;

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
        return mainPage.finish();
    }

}

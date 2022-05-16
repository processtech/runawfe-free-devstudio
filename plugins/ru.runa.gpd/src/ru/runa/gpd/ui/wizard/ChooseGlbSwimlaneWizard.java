package ru.runa.gpd.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.IOUtils;

public class ChooseGlbSwimlaneWizard extends Wizard {
    private ChooseGlbSwimlaneWizardPage mainPage;
    private IStructuredSelection selection;
    private List<IContainer> processContainers;
    private ProcessDefinition processDefinition;
    private IContainer initialSelection;
    private IFolder parentProcessDefinitionFolder;
    private ProcessDefinition parentProcessDefinition;

    public ChooseGlbSwimlaneWizard(ProcessDefinition processDefinition, IStructuredSelection selection) {
        this.processDefinition = processDefinition;
        this.selection = selection;
        IFile file = processDefinition.getFile();
        this.initialSelection = (IContainer) IOUtils.getProcessSelectionResource(selection);
        parentProcessDefinitionFolder = (IFolder) (file == null ? null : file.getParent());
        this.processContainers = IOUtils.getAllProcessContainers();
        this.initialSelection = parentProcessDefinitionFolder.getParent();
        mainPage = new ChooseGlbSwimlaneWizardPage(processDefinition, this.initialSelection);
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

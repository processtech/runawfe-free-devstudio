package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class CompareProcessDefinitionWizard extends Wizard implements IImportWizard {

    private CompareProcessDefinitionWizardPage mainPage;

    @Override
    public boolean performFinish() {
        return mainPage.performFinish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        mainPage = new CompareProcessDefinitionWizardPage(selection);
    }

    @Override
    public void addPages() {
        addPage(mainPage);
    }

    @Override
    public boolean canFinish() {
        return mainPage.isValid();
    }

}

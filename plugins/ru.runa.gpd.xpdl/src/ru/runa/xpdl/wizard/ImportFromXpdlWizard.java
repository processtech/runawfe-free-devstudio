package ru.runa.xpdl.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportFromXpdlWizard extends Wizard implements IImportWizard {
    private ImportFromXpdlWizardPage mainPage;

    @Override
    public boolean performFinish() {
        return mainPage.performFinish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        mainPage = new ImportFromXpdlWizardPage(selection);
    }

    @Override
    public void addPages() {
        addPage(mainPage);
    }
}

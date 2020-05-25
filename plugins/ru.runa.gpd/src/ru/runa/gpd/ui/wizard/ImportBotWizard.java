package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportBotWizard extends Wizard implements IImportWizard {
    private ImportBotElementWizardPage page;

    public ImportBotWizard(ImportBotElementWizardPage page) {
        this.page = page;
    }

    @Override
    public boolean performFinish() {
        return page.performFinish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        addPage(page);
    }
}

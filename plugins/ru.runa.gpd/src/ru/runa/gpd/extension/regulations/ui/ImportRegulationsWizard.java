package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportRegulationsWizard extends Wizard implements IImportWizard {
    private ImportRegulationsWizardPage mainPage;

    @Override
    public boolean performFinish() {
        return mainPage.performFinish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("import regulations");
        mainPage = new ImportRegulationsWizardPage();
    }

    @Override
    public void addPages() {
        addPage(mainPage);
    }
}

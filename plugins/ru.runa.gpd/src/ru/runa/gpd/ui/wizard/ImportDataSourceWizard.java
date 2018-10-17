package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Localization;

public class ImportDataSourceWizard extends Wizard implements IImportWizard {

    private ImportDataSourceWizardPage mainPage;

    @Override
    public boolean performFinish() {
        return mainPage.performFinish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Localization.getString("ImportDataSourceWizard.wizard.title"));
        mainPage = new ImportDataSourceWizardPage(Localization.getString("ImportDataSourceWizard.wizard.title"), selection);
    }

    @Override
    public void addPages() {
        addPage(mainPage);
    }

}

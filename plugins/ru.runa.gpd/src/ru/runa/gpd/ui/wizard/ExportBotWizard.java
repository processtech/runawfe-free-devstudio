package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class ExportBotWizard extends Wizard implements IExportWizard {
    private ExportBotElementWizardPage page;

    public ExportBotWizard(ExportBotElementWizardPage page) {
        this.page = page;
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("BotExportWizard");
        if (section == null) {
            section = workbenchSettings.addNewSection("BotExportWizard");
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Localization.getString("ExportParWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        return page.finish();
    }
}

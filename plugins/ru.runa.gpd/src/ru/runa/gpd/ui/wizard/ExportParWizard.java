package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class ExportParWizard extends Wizard implements IExportWizard {
    private IStructuredSelection selection;
    private ExportParWizardPage page;

    public ExportParWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ParExportWizard");
        if (section == null) {
            section = workbenchSettings.addNewSection("ParExportWizard");
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setWindowTitle(Localization.getString("ExportParWizard.wizard.title"));
    }

    @Override
    public void addPages() {
        page = new ExportParWizardPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        return page.finish();
    }
}

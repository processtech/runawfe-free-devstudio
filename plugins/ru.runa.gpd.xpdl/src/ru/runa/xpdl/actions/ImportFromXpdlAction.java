package ru.runa.xpdl.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.ui.action.BaseActionDelegate;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.xpdl.wizard.ImportFromXpdlWizard;

public class ImportFromXpdlAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        ImportFromXpdlWizard wizard = new ImportFromXpdlWizard();
        wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
    }
}

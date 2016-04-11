package ru.runa.xpdl.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.ui.action.BaseActionDelegate;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.xpdl.wizard.ImportFromXPDLWizard;

public class ImportFromXPDLAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        ImportFromXPDLWizard wizard = new ImportFromXPDLWizard();
        wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
    }
}

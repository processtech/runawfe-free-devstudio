package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.ui.wizard.ExportProjectWizard;

public class ExportProject extends BaseActionDelegate{

	@Override
	public void run(IAction action) {
		ExportProjectWizard wizard = new ExportProjectWizard();
		wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
		CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
	}

}

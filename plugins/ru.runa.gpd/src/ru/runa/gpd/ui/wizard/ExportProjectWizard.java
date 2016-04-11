package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Localization;

public class ExportProjectWizard extends Wizard implements IExportWizard{

	private ExportProjectWizardPage page;
	private IStructuredSelection selection;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(Localization.getString("ExportProjectWizard.page.title"));
	}
	
	@Override
	public void addPages() {
		page = new ExportProjectWizardPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		return page.finish();
	}

}

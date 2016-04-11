package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.Localization;

public class ExternalProjectImportWizard extends Wizard implements
		IImportWizard {

	private ExternalProjectImportWizardPage mainPage;
	
	public ExternalProjectImportWizard() {
		this(null);
	}

	public ExternalProjectImportWizard(String initialPath) {
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		super.addPages();
		mainPage = new ExternalProjectImportWizardPage("wizardExternalProjectsPage"); //$NON-NLS-1$
		addPage(mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle(Localization.getString("ImportProjectWizard.page.title"));
	}

	public boolean performCancel() {
		mainPage.performCancel();
		return true;
	}

	public boolean performFinish() {
		return mainPage.createProjects();
	}

}

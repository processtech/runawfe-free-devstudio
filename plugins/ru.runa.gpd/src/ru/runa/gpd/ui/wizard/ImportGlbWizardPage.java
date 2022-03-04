package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import ru.runa.gpd.Localization;

public class ImportGlbWizardPage extends ImportParWizardPage {

    public ImportGlbWizardPage(String pageName, IStructuredSelection selection) {
        super(ImportGlbWizardPage.class, selection);
        setTitle(Localization.getString("ImportGlbWizardPage.page.title"));
        setDescription(Localization.getString("ImportGlbWizardPage.page.description"));
    }
    @Override
    public String  fileExtension() {
    	return ".glb";
    }
    
}

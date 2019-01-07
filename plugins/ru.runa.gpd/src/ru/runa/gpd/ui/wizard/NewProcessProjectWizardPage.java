package ru.runa.gpd.ui.wizard;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.FileNameChecker;

public class NewProcessProjectWizardPage extends WizardNewProjectCreationPage {

    public NewProcessProjectWizardPage(String pageName) {
        super(pageName);
    }

    @Override
    protected boolean validatePage() {
        if (!FileNameChecker.isValid(getProjectName())) {
            setErrorMessage(Localization.getString("error.project_name_not_valid"));
            return false;
        }
        return super.validatePage();
    }

}

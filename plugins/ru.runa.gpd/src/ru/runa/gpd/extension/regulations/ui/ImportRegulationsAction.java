package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.action.BaseActionDelegate;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;

public class ImportRegulationsAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        ImportRegulationsWizard wizard = new ImportRegulationsWizard();
        wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(CommonPreferencePage.isRegulationsMenuItemsEnabled() && editor != null
                && !(editor.getDefinition() instanceof SubprocessDefinition));
    }
}

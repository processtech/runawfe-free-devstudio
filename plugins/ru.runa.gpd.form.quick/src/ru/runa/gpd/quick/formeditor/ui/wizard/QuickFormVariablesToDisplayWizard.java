package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.List;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;

public class QuickFormVariablesToDisplayWizard extends Wizard implements INewWizard {
    private FormNode formNode;
    private List<QuickFormComponent> quickFormVariableDefs;
    private QuickFormVariablesToDisplayWizardPage page;

    public QuickFormVariablesToDisplayWizard(FormNode formNode, List<QuickFormComponent> templatedVariableDefs) {
        this.formNode = formNode;
        this.quickFormVariableDefs = templatedVariableDefs;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new QuickFormVariablesToDisplayWizardPage(formNode, quickFormVariableDefs);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        quickFormVariableDefs.clear();
        quickFormVariableDefs.addAll(page.getSelectedVariables());
        return true;
    }
}
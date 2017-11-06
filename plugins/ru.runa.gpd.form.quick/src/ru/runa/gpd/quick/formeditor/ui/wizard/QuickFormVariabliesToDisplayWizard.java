package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;

public class QuickFormVariabliesToDisplayWizard extends Wizard implements INewWizard {
    private FormNode formNode;
    private List<QuickFormGpdVariable> quickFormVariableDefs;
    private QuickFormVariabliesToDisplayWizardPage page;

    public QuickFormVariabliesToDisplayWizard(FormNode formNode, List<QuickFormGpdVariable> templatedVariableDefs) {
        this.formNode = formNode;
        this.quickFormVariableDefs = templatedVariableDefs;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new QuickFormVariabliesToDisplayWizardPage(formNode, quickFormVariableDefs);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
    	quickFormVariableDefs.addAll(page.getSelectedVariables());
        return true;
    }

}

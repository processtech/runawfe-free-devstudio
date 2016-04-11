package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.quick.formeditor.QuickFormType;
import ru.runa.gpd.util.VariableUtils;

public class QuickFormVariabliesToDisplayWizard extends Wizard implements INewWizard {
    private ProcessDefinition processDefinition;
    private List<QuickFormGpdVariable> quickFormVariableDefs;
    private QuickFormVariabliesToDisplayWizardPage page;

    public QuickFormVariabliesToDisplayWizard(ProcessDefinition processDefinition, List<QuickFormGpdVariable> templatedVariableDefs) {
        this.processDefinition = processDefinition;
        this.quickFormVariableDefs = templatedVariableDefs;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new QuickFormVariabliesToDisplayWizardPage(processDefinition, quickFormVariableDefs);

        addPage(page);
    }

    @Override
    public boolean performFinish() {
    	quickFormVariableDefs.addAll(page.getSelectedVariables());
        return true;
    }

}

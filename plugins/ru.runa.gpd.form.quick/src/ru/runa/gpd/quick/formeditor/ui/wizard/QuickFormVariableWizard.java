package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class QuickFormVariableWizard extends Wizard implements INewWizard {
    private final FormNode formNode;
    private QuickFormVariableWizardPage page;
    private int editIndex = -1;
    private final List<QuickFormGpdVariable> quickFormVariableDefs;

    public QuickFormVariableWizard(ProcessDefinition processDefinition, FormNode formNode, List<QuickFormGpdVariable> templatedVariableDefs,
            int editIndex) {
        this.formNode = formNode;
        this.quickFormVariableDefs = templatedVariableDefs;
        this.editIndex = editIndex;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new QuickFormVariableWizardPage(formNode, editIndex != -1 ? quickFormVariableDefs.get(editIndex) : null);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        QuickFormGpdVariable variableDef = null;
        if (editIndex != -1) {
            variableDef = quickFormVariableDefs.get(editIndex);
        } else {
            variableDef = new QuickFormGpdVariable();
        }
        variableDef.setTagName(page.getTagType());
        Variable variable = page.getVariable();
        Preconditions.checkNotNull(variable, "Variable is null");
        variableDef.setName(variable.getName());
        variableDef.setScriptingName(variable.getScriptingName());
        variableDef.setDescription(variable.getDescription());
        variableDef.setFormatLabel(variable.getFormatLabel());
        variableDef.setParams(null);
        if (page.getParamValue() != null && !page.getParamValue().isEmpty()) {
            List<String> param = Lists.newArrayListWithExpectedSize(1);
            param.add(page.getParamValue());
            variableDef.setParams(param.toArray(new String[0]));
        }
        if (editIndex == -1) {
            quickFormVariableDefs.add(variableDef);
        }
        return true;
    }
}

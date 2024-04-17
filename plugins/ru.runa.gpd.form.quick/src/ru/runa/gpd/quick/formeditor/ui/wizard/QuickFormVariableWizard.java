package ru.runa.gpd.quick.formeditor.ui.wizard;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;

public class QuickFormVariableWizard extends Wizard implements INewWizard {
    private final FormNode formNode;
    private QuickFormVariableWizardPage page;
    private int editIndex = -1;
    private final List<QuickFormComponent> quickFormVariableDefs;

    public QuickFormVariableWizard(FormNode formNode, List<QuickFormComponent> templatedVariableDefs, int editIndex) {
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
        QuickFormComponent variableDef = null;
        if (editIndex != -1) {
            variableDef = quickFormVariableDefs.get(editIndex);
        } else {
            variableDef = new QuickFormComponent();
        }
        variableDef.setTagName(page.getTagType());
        Variable variable = page.getVariable();
        Preconditions.checkNotNull(variable, "Variable is null");
        variableDef.fillFromVariable(variable);
        variableDef.setParams(page.getParamsValues());
        if (editIndex == -1) {
            quickFormVariableDefs.add(variableDef);
        }
        return true;
    }
}

package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefGroup;

public class BotTaskParamDefWizard extends Wizard implements INewWizard {
    private BotTaskParamDefWizardPage page;
    private ParamDefGroup paramDefGroup;
    private ParamDef paramDef;

    public BotTaskParamDefWizard(ParamDefGroup paramDefGroup, ParamDef paramDef) {
        this.paramDefGroup = paramDefGroup;
        this.paramDef = paramDef;
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new BotTaskParamDefWizardPage(paramDefGroup, paramDef);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        if (page.getParamDef() != null) {
            paramDefGroup.getParameters().remove(paramDef);
        }
        ParamDef paramDef = new ParamDef(page.getName(), page.getName());
        paramDef.getFormatFilters().add(page.getType());
        paramDef.setUseVariable(page.isUseVariable());
        paramDef.setOptional(page.isOptional());
        paramDefGroup.getParameters().add(paramDef);
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }
}

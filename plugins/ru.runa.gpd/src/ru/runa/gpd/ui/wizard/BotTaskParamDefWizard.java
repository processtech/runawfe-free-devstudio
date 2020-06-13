package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class BotTaskParamDefWizard extends Wizard implements INewWizard {
    private BotTaskParamDefWizardPage page;
    private ParamDefGroup paramDefGroup;
    private ParamDef paramDef;
    private final DialogEnhancementMode dialogEnhancementMode;

    public BotTaskParamDefWizard(ParamDefGroup paramDefGroup, ParamDef paramDef, DialogEnhancementMode dialogEnhancementMode) {
        this.paramDefGroup = paramDefGroup;
        this.paramDef = paramDef;
        this.dialogEnhancementMode = dialogEnhancementMode;
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new BotTaskParamDefWizardPage(paramDefGroup, paramDef, dialogEnhancementMode);
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

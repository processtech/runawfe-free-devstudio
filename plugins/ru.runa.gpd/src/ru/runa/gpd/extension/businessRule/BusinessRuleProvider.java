package ru.runa.gpd.extension.businessRule;

import org.eclipse.jface.window.Window;
import ru.runa.gpd.extension.GroovyBasedProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class BusinessRuleProvider extends GroovyBasedProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        BusinessRuleEditorDialog dialog = new BusinessRuleEditorDialog(definition, delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }
}

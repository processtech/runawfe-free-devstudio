package ru.runa.gpd.extension;

import org.eclipse.jface.window.Window;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.ConditionalExpressionDialog;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class ConditionalExpressionProvider extends DelegableProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode mode) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        ConditionalExpressionDialog dialog = new ConditionalExpressionDialog(definition, delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }
}

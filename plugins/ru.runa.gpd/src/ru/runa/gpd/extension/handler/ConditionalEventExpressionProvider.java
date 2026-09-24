package ru.runa.gpd.extension.handler;

import java.util.List;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.extension.GroovyBasedProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.ltk.ConditionalEventDelegablePresentation;
import ru.runa.gpd.ui.dialog.ConditionalExpressionDialog;
import ru.runa.gpd.ui.enhancement.ConditionalEventExpressionDelegableAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

/**
 * Provider for conditional event expressions configuration.
 * <p>
 * This class does not provide custom variable rename refactoring.
 * Variable rename refactoring is implemented in
 * {@link ConditionalEventDelegablePresentation}.
 */
public class ConditionalEventExpressionProvider extends GroovyBasedProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode mode) {
        ProcessDefinition definition = ((ProcessDefinitionAware) delegable).getProcessDefinition();
        ConditionalExpressionDialog dialog = new ConditionalExpressionDialog(definition, delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) {
        return super.validateValue(adapt(delegable), errors);
    }

    private ConditionalEventExpressionDelegableAdapter adapt(Delegable delegable) {
        return new ConditionalEventExpressionDelegableAdapter((CatchEventNode) delegable);
    }
}

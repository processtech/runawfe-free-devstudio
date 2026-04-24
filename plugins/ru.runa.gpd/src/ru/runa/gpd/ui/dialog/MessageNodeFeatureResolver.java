package ru.runa.gpd.ui.dialog;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.editor.graphiti.change.ChangeDelegationConfigurationFeature;
import ru.runa.gpd.editor.graphiti.change.ChangePropertyFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeVariableMappingsFeature;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.util.VariableMapping;

/**
 * Entry point for resolving configuration dialogs for {@link MessageNode}.
 *
 * <p>For conditional events (see {@link CatchEventNode}),
 * selects dialog based on configuration type (expression or external storage).
 */
public final class MessageNodeFeatureResolver {

    private MessageNodeFeatureResolver() {
    }

    public static ChangePropertyFeature<?, ?> resolveFeature(MessageNode messageNode) {
        if (messageNode instanceof CatchEventNode && ((CatchEventNode) messageNode).isConditional()) {
            return resolveConditionalEventFeature((CatchEventNode) messageNode);
        }
        return createRegularMessageNodeFeature(messageNode);
    }

    private static ChangePropertyFeature<?, ?> resolveConditionalEventFeature(CatchEventNode node) {
        if (node.isUseExternalStorageIn()) {
            return createExternalStorageConfigurationFeature(node);
        }
        return createConditionalExpressionConfigurationFeature(node);
    }

    private static ChangePropertyFeature<?, ?> createExternalStorageConfigurationFeature(CatchEventNode catchEventNode) {
        String oldValue = catchEventNode.getDelegationConfiguration();
        String newValue = DialogEnhancement.showConfigurationDialog(catchEventNode);
        if (newValue != null && !newValue.equals(oldValue)) {
            return new ChangeDelegationConfigurationFeature(catchEventNode, oldValue, newValue);
        }
        return null;
    }

    private static ChangePropertyFeature<?, ?> createConditionalExpressionConfigurationFeature(CatchEventNode catchEventNode) {
        ConditionalExpressionDialog dialog = new ConditionalExpressionDialog(catchEventNode);
        if (dialog.open() == Window.OK) {
            String oldValue = catchEventNode.getDelegationConfiguration();
            String newValue = dialog.getResult();
            if (!Objects.equal(newValue, oldValue)) {
                return new ChangeDelegationConfigurationFeature(catchEventNode, oldValue, newValue);
            }
        }
        return null;
    }

    private static ChangePropertyFeature<?, ?> createRegularMessageNodeFeature(MessageNode messageNode) {
        List<VariableMapping> oldMappings = messageNode.getVariableMappings();
        MessageNodeDialog dialog = new MessageNodeDialog(
                messageNode.getProcessDefinition(),
                messageNode.getVariableMappings(),
                false,
                messageNode.getName()
        );
        if (dialog.open() != Window.CANCEL) {
            if (!Objects.equal(dialog.getVariableMappings(), oldMappings)) {
                return new ChangeVariableMappingsFeature(messageNode, dialog.getVariableMappings());
            }
        }
        return null;
    }
}

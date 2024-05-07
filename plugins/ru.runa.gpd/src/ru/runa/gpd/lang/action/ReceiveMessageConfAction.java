package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.editor.graphiti.change.ChangeVariableMappingsFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;
import ru.runa.gpd.util.VariableMapping;

public class ReceiveMessageConfAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        MessageNode messageNode = getSelection();
        List<VariableMapping> oldMappings = messageNode.getVariableMappings();
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(), false,
                messageNode.getName());
        if (dialog.open() != Window.CANCEL) {
            if (!Objects.equal(dialog.getVariableMappings(), oldMappings)) {
                UndoRedoUtil.executeFeature(new ChangeVariableMappingsFeature(messageNode, dialog.getVariableMappings()));
            }
        }
    }
}

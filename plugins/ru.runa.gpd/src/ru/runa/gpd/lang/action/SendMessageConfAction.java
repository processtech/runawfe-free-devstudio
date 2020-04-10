package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.editor.graphiti.ChangeVariableMappingsFeature;
import ru.runa.gpd.editor.graphiti.UndoRedoUtil;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;
import ru.runa.gpd.util.VariableMapping;

public class SendMessageConfAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        MessageNode messageNode = getSelection();
        List<VariableMapping> oldMappings = messageNode.getVariableMappings();
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(), true,
                messageNode.getName());
        if (dialog.open() != Window.CANCEL) {
            if (!Objects.equal(dialog.getVariableMappings(), oldMappings)) {
                UndoRedoUtil.executeFeature(new ChangeVariableMappingsFeature(messageNode, dialog.getVariableMappings()));
            }
        }
    }
}

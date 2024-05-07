package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.graphiti.change.ChangeTtlDurationFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class SendMessageEditTTLAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        MessageNode messageNode = getSelection();
        Duration oldDuration = messageNode.getTtlDuration();
        DurationEditDialog dialog = new DurationEditDialog(messageNode.getProcessDefinition(), messageNode.getTtlDuration());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
            if (!Objects.equal(result, oldDuration)) {
                UndoRedoUtil.executeFeature(new ChangeTtlDurationFeature(messageNode, result));
            }
        }
    }
}

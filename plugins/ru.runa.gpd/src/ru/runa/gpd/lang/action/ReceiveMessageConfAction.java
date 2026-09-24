package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.graphiti.change.ChangePropertyFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.ui.dialog.MessageNodeFeatureResolver;

public class ReceiveMessageConfAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        ChangePropertyFeature<?, ?> feature = MessageNodeFeatureResolver.resolveFeature(getSelection());
        if (feature != null) {
            UndoRedoUtil.executeFeature(feature);
        }
    }
}

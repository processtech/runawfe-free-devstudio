package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.editor.graphiti.change.ChangePropertyFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.ui.dialog.MessageNodeFeatureResolver;

public class DoubleClickEventNodeFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return getBusinessObject(context) instanceof AbstractEventNode && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        ChangePropertyFeature<?, ?> feature =
                MessageNodeFeatureResolver.resolveFeature(
                        (MessageNode) getBusinessObject(context));

        if (feature != null) {
            Display.getDefault().asyncExec(() ->
                    UndoRedoUtil.executeFeature(feature));
        }
    }
}

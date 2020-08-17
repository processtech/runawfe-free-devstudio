package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;
import ru.runa.gpd.lang.model.jpdl.SendMessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

public class DoubleClickEventNodeFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return getBusinessObject(context) instanceof AbstractEventNode && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        MessageNode messageNode = (MessageNode) getBusinessObject(context);
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(),
                messageNode instanceof ThrowEventNode || messageNode instanceof SendMessageNode, messageNode.getName());
        if (dialog.open() != Window.CANCEL) {
            messageNode.setVariableMappings(dialog.getVariableMappings());
        }
    }

}

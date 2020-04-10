package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.editor.graphiti.ChangeVariableMappingsFeature;
import ru.runa.gpd.editor.graphiti.UndoRedoUtil;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;
import ru.runa.gpd.lang.model.jpdl.SendMessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;
import ru.runa.gpd.util.VariableMapping;

public class DoubleClickEventNodeFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement()) instanceof AbstractEventNode && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        MessageNode messageNode = (MessageNode) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        List<VariableMapping> oldMappings = messageNode.getVariableMappings();
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(),
                messageNode instanceof ThrowEventNode || messageNode instanceof SendMessageNode, messageNode.getName());
        if (dialog.open() != Window.CANCEL) {
            if (!Objects.equal(dialog.getVariableMappings(), oldMappings)) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UndoRedoUtil.executeFeature(new ChangeVariableMappingsFeature(messageNode, dialog.getVariableMappings()));
                    }
                });
            }
        }
    }

}

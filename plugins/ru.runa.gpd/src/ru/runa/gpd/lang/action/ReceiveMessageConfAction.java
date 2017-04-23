package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

public class ReceiveMessageConfAction extends BaseModelActionDelegate {
	@Override
	public void run(IAction action) {
		MessageNode messageNode = getMessageNode(getSelection());
		if (messageNode == null) {
			action.setEnabled(false);
		} else {
			MessageNodeDialog dialog = new MessageNodeDialog(
					messageNode.getProcessDefinition(),
					messageNode.getVariableMappings(), false);
			if (dialog.open() != Window.CANCEL) {
				messageNode.setVariableMappings(dialog.getVariableMappings());
			}
		}
	}

	private MessageNode getMessageNode(final Object selection) {
		if (selection instanceof MessageNode) {
			return (MessageNode) selection;
		} else if (selection instanceof TaskState) {
			return ((TaskState) selection).getCatchEventNodes();
		}
		return null;
	}

	@Override
    public void selectionChanged(IAction action, ISelection selection) {		
		super.selectionChanged(action, selection);
		action.setEnabled(getMessageNode(getSelection()) != null);		
    }
	
}

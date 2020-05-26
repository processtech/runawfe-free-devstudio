package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.gef.commands.Command;
import org.eclipse.graphiti.features.IFeatureHolder;
import org.eclipse.graphiti.internal.command.CommandContainer;
import org.eclipse.graphiti.internal.command.GFPreparableCommand2;
import org.eclipse.graphiti.internal.command.ICommand;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.graphiti.ui.internal.editor.EmfOnGefCommand;
import org.eclipse.graphiti.ui.internal.editor.GFCommandStack;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;

public class CustomCommandStack extends GFCommandStack implements IOperationHistoryListener {

    private TransactionalCommandStack emfCommandStack;

    public CustomCommandStack(IConfigurationProvider configurationProvider, TransactionalEditingDomain editingDomain) {
        super(configurationProvider, editingDomain);
        emfCommandStack = (TransactionalCommandStack) editingDomain.getCommandStack();
        ((IWorkspaceCommandStack) emfCommandStack).getOperationHistory().addOperationHistoryListener(this);
    }

    @Override
    public void execute(Command gefCommand) {
        if (hasDoneChanges(gefCommand)) {
            flush();
        }
        super.execute(gefCommand);
    }

    private boolean hasDoneChanges(Command gefCommand) {
        if (gefCommand != null) {
            if (gefCommand instanceof IFeatureHolder) {
                return ((IFeatureHolder) gefCommand).getFeature().hasDoneChanges();
            } else if (gefCommand instanceof GefCommandWrapper) {
                ICommand graphitiCommand = ((GefCommandWrapper) gefCommand).getCommand();
                if (graphitiCommand instanceof IFeatureHolder) {
                    return ((IFeatureHolder) graphitiCommand).getFeature().hasDoneChanges();
                } else if (graphitiCommand instanceof CommandContainer) {
                    for (ICommand command : ((CommandContainer) graphitiCommand).getCommands()) {
                        if (command instanceof IFeatureHolder) {
                            if (((IFeatureHolder) command).getFeature().hasDoneChanges()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void flush() {
        super.flush();
        emfCommandStack.flush();
    }

    @Override
    public void dispose() {
        super.dispose();
        ((IWorkspaceCommandStack) emfCommandStack).getOperationHistory().removeOperationHistoryListener(this);
        emfCommandStack = null;
    }

    @Override
    public void historyNotification(OperationHistoryEvent event) {
        switch(event.getEventType()) {
        case OperationHistoryEvent.UNDONE:
            gefCommand(event).undo();
            break;
        case OperationHistoryEvent.REDONE:
            gefCommand(event).redo();
            break;
        }
    }

    private Command gefCommand(OperationHistoryEvent event) {
        return ((EmfOnGefCommand) ((GFPreparableCommand2) ((EMFCommandOperation) event.getOperation()).getCommand()).getCommand()).getGefCommand();
    }

}

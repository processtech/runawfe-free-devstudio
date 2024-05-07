package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IFeatureHolder;
import org.eclipse.graphiti.internal.command.CommandContainer;
import org.eclipse.graphiti.internal.command.FeatureCommand;
import org.eclipse.graphiti.internal.command.GFPreparableCommand2;
import org.eclipse.graphiti.internal.command.ICommand;
import org.eclipse.graphiti.ui.internal.command.AbstractCommand;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.graphiti.ui.internal.editor.EmfOnGefCommand;
import org.eclipse.graphiti.ui.internal.editor.GFCommandStack;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;

public class CustomCommandStack extends GFCommandStack implements IOperationHistoryListener {

    private TransactionalCommandStack emfCommandStack;
    final private UndoContext undoContext = new UndoContext();

    public CustomCommandStack(IConfigurationProvider configurationProvider, TransactionalEditingDomain editingDomain) {
        super(configurationProvider, editingDomain);
        emfCommandStack = (TransactionalCommandStack) editingDomain.getCommandStack();
        ((IWorkspaceCommandStack) emfCommandStack).getOperationHistory().addOperationHistoryListener(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        ((IWorkspaceCommandStack) emfCommandStack).getOperationHistory().removeOperationHistoryListener(this);
        emfCommandStack = null;
    }

    @Override
    public void historyNotification(OperationHistoryEvent event) {
        if (event.getEventType() == OperationHistoryEvent.DONE) {
            IUndoableOperation operation = event.getOperation();
            if (operation instanceof EMFCommandOperation) {
                Command gefCommand = gefCommand(event);
                if (gefCommand instanceof AbstractCommand) {
                    operation.addContext(undoContext);
                } else {
                    IFeature feature = getFeature(gefCommand);
                    if (feature instanceof IRedoProtected) {
                        operation.addContext(undoContext);
                    }
                    if (feature instanceof IDoneChangesFeature) {
                        ((IDoneChangesFeature) feature).setHasDoneChanges(false);
                    }
                }
            }
        }
    }

    private Command gefCommand(OperationHistoryEvent event) {
        org.eclipse.emf.common.command.Command emfCommand = ((EMFCommandOperation) event.getOperation()).getCommand();
        if (emfCommand instanceof GFPreparableCommand2) {
            return ((EmfOnGefCommand) ((GFPreparableCommand2) emfCommand).getCommand()).getGefCommand();
        }
        return null;
    }

    private IFeature getFeature(Command gefCommand) {
        if (gefCommand instanceof GefCommandWrapper) {
            ICommand command = ((GefCommandWrapper) gefCommand).getCommand();
            if (command instanceof FeatureCommand) {
                return ((FeatureCommand) command).getFeature();
            }

            if (command instanceof CommandContainer) {
                for (ICommand c : ((CommandContainer) command).getCommands()) {
                    if (c instanceof IFeatureHolder) {
                        return ((IFeatureHolder) c).getFeature();
                    }
                }
            }
        }
        if (gefCommand instanceof CompoundCommand) {
            for (Object c : ((CompoundCommand) gefCommand).getCommands()) {
                if (c instanceof GefCommandWrapper) {
                    return getFeature((Command) c);
                }
            }
        }
        return null;
    }

    @Override
    public void undo() {
        UndoRedoUtil.setInProgress(true);
        super.undo();
        UndoRedoUtil.setInProgress(false);
    }

    @Override
    public void redo() {
        UndoRedoUtil.setInProgress(true);
        super.redo();
        UndoRedoUtil.setInProgress(false);
    }

}

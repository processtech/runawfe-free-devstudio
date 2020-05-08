package ru.runa.gpd.editor.graphiti;

import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.graphiti.features.IFeatureHolder;
import org.eclipse.graphiti.internal.command.CommandContainer;
import org.eclipse.graphiti.internal.command.ICommand;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.graphiti.ui.internal.editor.GFCommandStack;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;

public class CustomCommandStack extends GFCommandStack {

    private TransactionalCommandStack emfCommandStack;

    public CustomCommandStack(IConfigurationProvider configurationProvider, TransactionalEditingDomain editingDomain) {
        super(configurationProvider, editingDomain);
        emfCommandStack = (TransactionalCommandStack) editingDomain.getCommandStack();
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
        emfCommandStack = null;
    }

}

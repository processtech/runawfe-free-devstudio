package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.IDelegable;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        IDelegable iDelegable = (IDelegable) getSelection();
        DelegableProvider provider = HandlerRegistry.getProvider(iDelegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(iDelegable);
        if (newConfig != null) {
            iDelegable.setDelegationConfiguration(newConfig);
        }
    }
}

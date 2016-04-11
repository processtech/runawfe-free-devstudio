package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.PlatformUI;


import ru.runa.gpd.BotCache;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.ui.wizard.ExternalProjectImportWizard;

public class ImportProject extends BaseActionDelegate{

	@Override
	public void run(IAction action) {
		
		try {
			
			ExternalProjectImportWizard wizard = new ExternalProjectImportWizard();
		    wizard.init(PlatformUI.getWorkbench(), null);
		    
		    CompactWizardDialog dialog = new CompactWizardDialog(wizard);
	        if (dialog.open() == IDialogConstants.OK_ID) {
	            BotCache.reload();
	        }
			
		} catch (Exception e) {
			PluginLogger.logError(e);
		} 
		
	}

}

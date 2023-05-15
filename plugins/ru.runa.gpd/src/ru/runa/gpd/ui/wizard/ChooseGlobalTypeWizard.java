package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.wizard.Wizard;

import ru.runa.gpd.lang.model.ProcessDefinition;

public class ChooseGlobalTypeWizard extends Wizard {
	private ChooseGlobalTypeWizardPage mainPage;	 
    private IContainer initialSelection;
    

    public ChooseGlobalTypeWizard(ProcessDefinition processDefinition) {                
        IFile file = processDefinition.getFile();        
        IFolder parentProcessDefinitionFolder = (IFolder) (file == null ? null : file.getParent());               
        this.initialSelection = (IContainer)parentProcessDefinitionFolder.getParent();
        mainPage = new ChooseGlobalTypeWizardPage(processDefinition, this.initialSelection);            
    }
    
	@Override
    public void addPages() {    	
    	addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }
}

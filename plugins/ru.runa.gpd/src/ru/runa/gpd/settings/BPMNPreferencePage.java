package ru.runa.gpd.settings;

import java.awt.Font;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;

public class BPMNPreferencePage extends FieldEditorPreferencePage implements PrefConstants, IWorkbenchPreferencePage {
	private static final String PREF_COMMON_BPMN = "pref.common.bpmn.";
	
    public BPMNPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
    	final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    	store.setDefault(P_BPMN_FONT, new FontData("Arial", 8, Font.PLAIN).toString());
    	addColorField(store, P_BPMN_COLOR_TRANSITION);
    }
    
    private void addColorField(IPreferenceStore store, String name) {
    	addField(new ColorFieldEditor(name, Localization.getString(PREF_COMMON_BPMN + name), getFieldEditorParent()));
    }
    
    @Override
    protected void contributeButtons(final Composite buttonBar) {
    	applyDialogFont(buttonBar);
    	}
    
    @Override
    public boolean performOk() {
    	boolean performOk = super.performOk();
    	if(performOk){
    		applyStyles();
    	}
    	return performOk;
    }
    
    @Override
    protected void performApply() {
    	super.performApply();
    	applyStyles();
    }
    
    private void applyStyles() {
    	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    	for (IEditorReference ref : page.getEditorReferences()) {
    		IEditorPart editor = ref.getEditor(true);
    		if (editor instanceof GraphitiProcessEditor) {
    			((GraphitiProcessEditor) editor).getDiagramEditorPage().applyStyles();
    			}
    		}
    }
}
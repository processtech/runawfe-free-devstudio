package ru.runa.gpd.htmleditor.template;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import ru.runa.gpd.EditorsPlugin;

/**
 * The preference page for HTML code completion templates.
 * 
 * @author Naoki Takezoe
 */
public class HTMLTemplatePreferencePage extends TemplatePreferencePage  implements IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public HTMLTemplatePreferencePage() {
		try {
			setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
			setTemplateStore(HTMLTemplateManager.getInstance().getTemplateStore());
			setContextTypeRegistry(HTMLTemplateManager.getInstance().getContextTypeRegistry());
		} catch(Exception ex){
		}
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	public boolean performOk() {
		boolean ok = super.performOk();
		EditorsPlugin.getDefault().savePluginPreferences();
		return ok;
	}

}

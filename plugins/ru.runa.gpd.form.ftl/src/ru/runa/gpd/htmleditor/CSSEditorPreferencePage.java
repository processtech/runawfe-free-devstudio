package ru.runa.gpd.htmleditor;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.EditorsPlugin;

/**
 * The preference page for the CSS editor.
 * 
 * @author Naoki Takezoe
 */
public class CSSEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private ColorFieldEditor colorComment;
	private ColorFieldEditor colorProperty;
	private ColorFieldEditor colorValue;
	
	public CSSEditorPreferencePage() {
		super(GRID); //$NON-NLS-1$
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}
	
	protected void createFieldEditors() {
		setTitle(HTMLPlugin.getResourceString("HTMLEditorPreferencePage.CSS"));
		
		Composite parent = getFieldEditorParent();
		
		colorComment = new ColorFieldEditor(HTMLPlugin.PREF_COLOR_CSSCOMMENT,
					HTMLPlugin.getResourceString("HTMLEditorPreferencePage.CSSCommentColor"),
					parent);
		addField(colorComment);
		
		colorProperty = new ColorFieldEditor(HTMLPlugin.PREF_COLOR_CSSPROP,
					HTMLPlugin.getResourceString("HTMLEditorPreferencePage.CSSPropColor"),
					parent);
		addField(colorProperty);
		
		colorValue = new ColorFieldEditor(HTMLPlugin.PREF_COLOR_CSSVALUE,
				HTMLPlugin.getResourceString("HTMLEditorPreferencePage.CSSValueColor"),
				parent);
		addField(colorValue);
	}

}

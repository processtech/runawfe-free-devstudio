package ru.runa.gpd.htmleditor.template;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

import ru.runa.gpd.EditorsPlugin;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLContextType extends TemplateContextType {
	
	public static final String CONTEXT_TYPE 
		= EditorsPlugin.getDefault().getBundle().getSymbolicName() + ".templateContextType.html";
	
	public HTMLContextType(){
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
	}
	
}

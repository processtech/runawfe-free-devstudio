package ru.runa.gpd.htmleditor.template;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class JavaScriptContextType extends TemplateContextType {
	
	public static final String CONTEXT_TYPE 
		= "EditorsPlugin.templateContextType.javascript";

	public JavaScriptContextType(){
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
	}
	
}

package ru.runa.gpd.jseditor;

import ru.runa.gpd.htmleditor.editors.HTMLSourceEditorContributer;

public class JavaScriptEditorContributor extends HTMLSourceEditorContributer {
	
	public JavaScriptEditorContributor(){
		addActionId(JavaScriptEditor.ACTION_COMMENT);
	}
	
}

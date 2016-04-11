package ru.runa.gpd.xmleditor;

import ru.runa.gpd.htmleditor.editors.HTMLSourceEditorContributer;

public class XMLEditorContributor extends HTMLSourceEditorContributer {
	
	public XMLEditorContributor(){
		addActionId(XMLEditor.ACTION_ESCAPE_XML);
		addActionId(XMLEditor.ACTION_COMMENT);
	}
	
}

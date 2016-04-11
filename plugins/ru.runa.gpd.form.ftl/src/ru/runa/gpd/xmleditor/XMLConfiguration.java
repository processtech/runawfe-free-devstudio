package ru.runa.gpd.xmleditor;

import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.assist.HTMLAssistProcessor;
import ru.runa.gpd.htmleditor.editors.HTMLConfiguration;

public class XMLConfiguration extends HTMLConfiguration {
	
//	private XMLAssistProcessor assistProcessor;
	
	public XMLConfiguration(ColorProvider colorProvider) {
		super(colorProvider);
	}
	
	protected HTMLAssistProcessor createAssistProcessor() {
		return new XMLAssistProcessor();
	}
	
}

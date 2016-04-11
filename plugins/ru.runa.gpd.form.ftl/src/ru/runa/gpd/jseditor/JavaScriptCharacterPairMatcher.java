package ru.runa.gpd.jseditor;

import org.eclipse.jface.text.IDocument;

import ru.runa.gpd.htmleditor.editors.AbstractCharacterPairMatcher;

public class JavaScriptCharacterPairMatcher extends AbstractCharacterPairMatcher { //implements ICharacterPairMatcher {
	
	public JavaScriptCharacterPairMatcher() {
		addQuoteCharacter('\'');
		addQuoteCharacter('"');
		addBlockCharacter('{', '}');
		addBlockCharacter('(', ')');
	}
	
	protected String getSource(IDocument doc){
		return JavaScriptUtil.removeComments(doc.get());
	}
	
}

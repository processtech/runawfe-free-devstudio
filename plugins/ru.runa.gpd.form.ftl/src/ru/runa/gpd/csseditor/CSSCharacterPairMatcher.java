package ru.runa.gpd.csseditor;

import org.eclipse.jface.text.IDocument;

import ru.runa.gpd.htmleditor.HTMLUtil;
import ru.runa.gpd.htmleditor.editors.AbstractCharacterPairMatcher;

/**
 * @author Naoki Takezoe
 */
public class CSSCharacterPairMatcher extends AbstractCharacterPairMatcher {

	public CSSCharacterPairMatcher() {
		addBlockCharacter('{', '}');
	}
	
	protected String getSource(IDocument document){
		return HTMLUtil.cssComment2space(document.get());
	}

}

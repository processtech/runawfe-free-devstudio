package ru.runa.gpd.xmleditor;

import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.editors.HTMLOutlinePage;

public class XMLOutlinePage extends HTMLOutlinePage {
	
	public XMLOutlinePage(XMLEditor editor) {
		super(editor);
	}
	
	protected Image getNodeImage(FuzzyXMLNode element){
		if(element instanceof FuzzyXMLElement){
			return HTMLPlugin.getImage(HTMLPlugin.ICON_TAG);
		}
		return super.getNodeImage(element);
	}
	
	protected boolean isHTML(){
		return false;
	}
}

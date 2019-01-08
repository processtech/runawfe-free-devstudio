package ru.runa.gpd.htmleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;

import ru.runa.gpd.csseditor.CSSBlockScanner;
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;

/**
 * <code>RuleBasedScanner</code> for the inner CSS in the HTML.
 * 
 * @author Naoki Takezoe
 */
public class InnerCSSScanner extends CSSBlockScanner {

	public InnerCSSScanner(ColorProvider colorProvider) {
		super(colorProvider);
	}
	
	protected List<IRule> createRules(ColorProvider colorProvider) {
		IToken tag = colorProvider.getToken(HTMLPlugin.PREF_COLOR_TAG);
		IToken comment = colorProvider.getToken(HTMLPlugin.PREF_COLOR_CSSCOMMENT);
		
		List<IRule> rules = new ArrayList<>();
		rules.add(new SingleLineRule("<style", ">", tag));
		rules.add(new SingleLineRule("</style", ">", tag));
		rules.add(new MultiLineRule("/*", "*/", comment));
		rules.addAll(super.createRules(colorProvider));
		return rules;
	}
}

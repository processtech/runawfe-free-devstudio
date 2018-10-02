package ru.runa.gpd.csseditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;

/**
 * @author Naoki Takezoe
 */
public class CSSBlockScanner extends RuleBasedScanner {
	
	public CSSBlockScanner(ColorProvider colorProvider) {
		List<IRule> rules = createRules(colorProvider);
		setRules(rules.toArray(new IRule[rules.size()]));
	}
	
	/**
	 * Creates the list of <code>IRule</code>.
	 * If you have to customize rules, override this method.
	 * 
	 * @param colorProvider ColorProvider
	 * @return the list of <code>IRule</code>
	 */
	protected List<IRule> createRules(ColorProvider colorProvider) {
		List<IRule> rules = new ArrayList<>();
		rules.add(new CSSRule(
				colorProvider.getToken(HTMLPlugin.PREF_COLOR_CSSPROP), 
				colorProvider.getToken(HTMLPlugin.PREF_COLOR_CSSVALUE)));
		return rules;
	}
	
}

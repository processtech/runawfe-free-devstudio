package ru.runa.gpd.htmleditor.editors;

import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;

import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.jseditor.JavaScriptScanner;

/**
 * <code>RuleBasedScanner</code> for the inner JavaScript in the HTML.
 * 
 * @author Naoki Takezoe
 */
public class InnerJavaScriptScanner extends JavaScriptScanner {

	public InnerJavaScriptScanner(ColorProvider colorProvider) {
		super(colorProvider);
	}

	protected List createRules(ColorProvider colorProvider) {
		IToken tag = colorProvider.getToken(HTMLPlugin.PREF_COLOR_TAG);
		IToken comment = colorProvider.getToken(HTMLPlugin.PREF_COLOR_JSCOMMENT);
		
		List rules = super.createRules(colorProvider);
		rules.add(new SingleLineRule("<script", ">", tag));
		rules.add(new SingleLineRule("</script", ">", tag));
		rules.add(new MultiLineRule("/*", "*/", comment));
		
		return rules;
	}
	
	
}

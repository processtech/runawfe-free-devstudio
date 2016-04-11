package ru.runa.gpd.htmleditor;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.htmleditor.editors.IHTMLColorConstants;

public class HTMLPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
		store.setDefault(HTMLPlugin.PREF_COLOR_TAG,StringConverter.asString(IHTMLColorConstants.TAG));
		store.setDefault(HTMLPlugin.PREF_COLOR_COMMENT,StringConverter.asString(IHTMLColorConstants.HTML_COMMENT));
		store.setDefault(HTMLPlugin.PREF_COLOR_DOCTYPE,StringConverter.asString(IHTMLColorConstants.PROC_INSTR));
		store.setDefault(HTMLPlugin.PREF_COLOR_STRING,StringConverter.asString(IHTMLColorConstants.STRING));
		store.setDefault(HTMLPlugin.PREF_COLOR_SCRIPT,StringConverter.asString(IHTMLColorConstants.SCRIPT));
		store.setDefault(HTMLPlugin.PREF_COLOR_CSSPROP,StringConverter.asString(IHTMLColorConstants.CSS_PROP));
		store.setDefault(HTMLPlugin.PREF_COLOR_CSSCOMMENT,StringConverter.asString(IHTMLColorConstants.CSS_COMMENT));
		store.setDefault(HTMLPlugin.PREF_COLOR_CSSVALUE,StringConverter.asString(IHTMLColorConstants.CSS_VALUE));
		store.setDefault(HTMLPlugin.PREF_EDITOR_TYPE,"tab");
		store.setDefault(HTMLPlugin.PREF_DTD_URI,"");
		store.setDefault(HTMLPlugin.PREF_DTD_PATH,"");
		store.setDefault(HTMLPlugin.PREF_DTD_CACHE,true);
		store.setDefault(HTMLPlugin.PREF_ASSIST_AUTO,true);
		store.setDefault(HTMLPlugin.PREF_ASSIST_CHARS,"</\"");
		store.setDefault(HTMLPlugin.PREF_ASSIST_CLOSE,true);
		store.setDefault(HTMLPlugin.PREF_ASSIST_TIMES,0);
		store.setDefault(HTMLPlugin.PREF_USE_SOFTTAB,false);
		store.setDefault(HTMLPlugin.PREF_SOFTTAB_WIDTH,2);
		store.setDefault(HTMLPlugin.PREF_COLOR_FG,StringConverter.asString(IHTMLColorConstants.FOREGROUND));
		store.setDefault(HTMLPlugin.PREF_COLOR_BG,StringConverter.asString(IHTMLColorConstants.BACKGROUND));
		store.setDefault(HTMLPlugin.PREF_COLOR_BG_DEF,true);
		store.setDefault(HTMLPlugin.PREF_JSP_COMMENT,StringConverter.asString(IHTMLColorConstants.JAVA_COMMENT));
		store.setDefault(HTMLPlugin.PREF_JSP_STRING,StringConverter.asString(IHTMLColorConstants.JAVA_STRING));
		store.setDefault(HTMLPlugin.PREF_JSP_KEYWORD,StringConverter.asString(IHTMLColorConstants.JAVA_KEYWORD));
		store.setDefault(HTMLPlugin.PREF_PAIR_CHAR, true);
		store.setDefault(HTMLPlugin.PREF_COLOR_JSCOMMENT,StringConverter.asString(IHTMLColorConstants.JAVA_COMMENT));
		store.setDefault(HTMLPlugin.PREF_COLOR_JSSTRING,StringConverter.asString(IHTMLColorConstants.JAVA_STRING));
		store.setDefault(HTMLPlugin.PREF_COLOR_JSKEYWORD,StringConverter.asString(IHTMLColorConstants.JAVA_KEYWORD));
		store.setDefault(HTMLPlugin.PREF_CUSTOM_ATTRS, "");
		store.setDefault(HTMLPlugin.PREF_CUSTOM_ELEMENTS, "");
		store.setDefault(HTMLPlugin.PREF_TASK_TAGS, "FIXME\t2\nTODO\t1\nXXXX\t1\n");
	}

}

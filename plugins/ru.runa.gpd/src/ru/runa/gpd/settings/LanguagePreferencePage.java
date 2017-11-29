package ru.runa.gpd.settings;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;

public class LanguagePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage, PrefConstants {

	public LanguagePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(
				P_DEFAULT_LANGUAGE,
				Localization.getString("pref.commons.defaultLanguage"),
				2,
				new String[][] {
						{ Language.JPDL.toString(), Language.JPDL.toString() },
						{ Language.BPMN.toString(), Language.BPMN.toString() } },
				getFieldEditorParent()));
	}
}

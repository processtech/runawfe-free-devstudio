package ru.runa.gpd.settings;

import java.text.SimpleDateFormat;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;

public class CommonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {

    public CommonPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new RadioGroupFieldEditor(P_DEFAULT_LANGUAGE, Localization.getString("pref.commons.defaultLanguage"), 2, new String[][] {
                { Language.JPDL.toString(), Language.JPDL.toString() }, { Language.BPMN.toString(), Language.BPMN.toString() } },
                getFieldEditorParent()));
        addField(new StringFieldEditor(P_DATE_FORMAT_PATTERN, Localization.getString("pref.commons.date.format"), getFieldEditorParent()) {
            @Override
            protected boolean doCheckState() {
                try {
                    new SimpleDateFormat(getStringValue());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        addField(new RadioGroupFieldEditor(P_ENABLE_REGULATIONS_MENU_ITEMS, Localization.getString("pref.commons.enableRegulationsMenuItems"), 2,
                new String[][] { { Localization.getString("disable"), Localization.getString("disable") },
                        { Localization.getString("enable"), Localization.getString("enable") } }, getFieldEditorParent()));
    }

    public static boolean isRegulationsMenuItemsEnabled() {
        boolean result = false;
        result = Activator.getDefault().getPreferenceStore().getString(P_ENABLE_REGULATIONS_MENU_ITEMS).equals(Localization.getString("enable")) ? true
                : false;
        return result;
    }
}

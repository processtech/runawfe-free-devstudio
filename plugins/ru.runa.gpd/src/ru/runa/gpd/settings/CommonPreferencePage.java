package ru.runa.gpd.settings;

import java.text.SimpleDateFormat;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

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
    }

}

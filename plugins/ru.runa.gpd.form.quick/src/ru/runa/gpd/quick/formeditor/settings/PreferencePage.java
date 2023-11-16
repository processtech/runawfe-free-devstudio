package ru.runa.gpd.quick.formeditor.settings;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.formeditor.settings.Localization;
import ru.runa.gpd.quick.Activator;

public class PreferencePage extends ru.runa.gpd.formeditor.settings.PreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        addField(new ComboFieldEditor(P_FORM_DEFAULT_DISPLAY_FORMAT, Localization.getString("pref.form.defaultDisplayFormat"), getNamesAndValues(),
                getFieldEditorParent()));
    }

}

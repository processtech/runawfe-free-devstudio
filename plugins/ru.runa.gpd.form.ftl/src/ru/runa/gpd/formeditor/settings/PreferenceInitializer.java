package ru.runa.gpd.formeditor.settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.EditorsPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = EditorsPlugin.getDefault().getPreferenceStore();
        store.setDefault(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT, "true");
    }

}

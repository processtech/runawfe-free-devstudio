package ru.runa.gpd.quick.formeditor.settings;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.quick.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT, "true");
    }

}

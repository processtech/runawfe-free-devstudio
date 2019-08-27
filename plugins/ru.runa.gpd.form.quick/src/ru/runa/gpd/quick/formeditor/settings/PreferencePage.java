package ru.runa.gpd.quick.formeditor.settings;

import org.eclipse.ui.IWorkbench;
import ru.runa.gpd.quick.Activator;

public class PreferencePage extends ru.runa.gpd.formeditor.settings.PreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

}

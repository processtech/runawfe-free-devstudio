package ru.runa.gpd.settings;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;

public class TasksTimeoutPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TasksTimeoutPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        //addField(new BooleanFieldEditor(PrefConstants.P_TASKS_TIMEOUT_ENABLED, Messages.getString("pref.escalation.enabled"), getFieldEditorParent()));
    }

}

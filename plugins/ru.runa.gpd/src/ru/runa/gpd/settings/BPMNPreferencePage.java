package ru.runa.gpd.settings;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class BPMNPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public BPMNPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(PrefConstants.P_BPMN_SHOW_SWIMLANE, Localization.getString("pref.notation.bpmn.showSwimlane"),
                getFieldEditorParent()));
    }

}
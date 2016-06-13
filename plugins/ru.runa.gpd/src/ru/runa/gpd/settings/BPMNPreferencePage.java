package ru.runa.gpd.settings;

import java.awt.Font;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class BPMNPreferencePage extends FieldEditorPreferencePage implements PrefConstants, IWorkbenchPreferencePage {

    private static final String PREF_COMMON_BPMN = "pref.common.bpmn.";

    public BPMNPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(P_BPMN_FONT, new FontData("Verdana", 9, Font.PLAIN).toString());
        addField(new FontFieldEditor(P_BPMN_FONT, Localization.getString(PREF_COMMON_BPMN + P_BPMN_FONT), getFieldEditorParent()));

        addColorField(store, P_BPMN_COLOR_FONT);
        addColorField(store, P_BPMN_COLOR_BACKGROUND);
        addColorField(store, P_BPMN_COLOR_BASE);
        addColorField(store, P_BPMN_COLOR_TRANSITION);
    }

    private void addColorField(IPreferenceStore store, String name) {
        addField(new ColorFieldEditor(name, Localization.getString(PREF_COMMON_BPMN + name), getFieldEditorParent()));
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        applyDialogFont(buttonBar);
    }
}
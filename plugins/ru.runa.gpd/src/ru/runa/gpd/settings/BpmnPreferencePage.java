package ru.runa.gpd.settings;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;

public class BpmnPreferencePage extends FieldEditorPreferencePage implements PrefConstants, IWorkbenchPreferencePage {
    public static final String LOCALIZATION_PREFIX = "pref.language.bpmn.";
    public static final String TOOLTIP_MODE_BASIC = "tooltip.basic";
    public static final String TOOLTIP_MODE_EXTENDED = "tooltip.extended";
    public static final String TOOLTIP_MODE_HIDDEN = "tooltip.hidden";

    public BpmnPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new FontFieldEditor(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT),
                Localization.getString(LOCALIZATION_PREFIX + P_BPMN_FONT), getFieldEditorParent()));
        addField(new ColorFieldEditor(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT_COLOR),
                Localization.getString(LOCALIZATION_PREFIX + P_BPMN_FONT_COLOR), getFieldEditorParent()));
        addField(new ColorFieldEditor(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_BACKGROUND_COLOR),
                Localization.getString(LOCALIZATION_PREFIX + P_BPMN_BACKGROUND_COLOR), getFieldEditorParent()));
        addField(new ColorFieldEditor(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FOREGROUND_COLOR),
                Localization.getString(LOCALIZATION_PREFIX + P_BPMN_FOREGROUND_COLOR), getFieldEditorParent()));
        addField(new IntegerFieldEditor(LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_LINE_WIDTH),
                Localization.getString(LOCALIZATION_PREFIX + P_BPMN_LINE_WIDTH), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_BPMN_EXPAND_CONTEXT_BUTTON_PAD, Localization.getString("pref.language.bpmn.expandContextButtonPad"),
                getFieldEditorParent()));
        addField(new ComboFieldEditor(P_BPMN_TOOLTIP, Localization.getString(LOCALIZATION_PREFIX + P_BPMN_TOOLTIP),
                getProtocolEntriesArray(), getFieldEditorParent()));
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        applyDialogFont(buttonBar);
    }

    @Override
    public boolean performOk() {
        super.performOk();
        GraphitiProcessEditor.refreshAllActiveEditors();
        return true;
    }

    private static String[][] getProtocolEntriesArray() {
        String[][] strings = new String[3][2];
        strings[0][0] = Localization.getString(LOCALIZATION_PREFIX + TOOLTIP_MODE_BASIC);
        strings[0][1] = TOOLTIP_MODE_BASIC;
        strings[1][0] = Localization.getString(LOCALIZATION_PREFIX + TOOLTIP_MODE_EXTENDED);
        strings[1][1] = TOOLTIP_MODE_EXTENDED;
        strings[2][0] = Localization.getString(LOCALIZATION_PREFIX + TOOLTIP_MODE_HIDDEN);
        strings[2][1] = TOOLTIP_MODE_HIDDEN;
        return strings;
    }

}

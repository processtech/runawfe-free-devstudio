package ru.runa.gpd.settings;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.aspects.UserActivity;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.util.UiUtil;

public class CommonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private IntegerFieldEditor savepointNumberEditor;
    private BooleanFieldEditor enableUserActivityLogging;

    public CommonPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new RadioGroupFieldEditor(P_DEFAULT_LANGUAGE, Localization.getString("pref.commons.defaultLanguage"), 2,
                new String[][] { { Language.JPDL.toString(), Language.JPDL.toString() }, { Language.BPMN.toString(), Language.BPMN.toString() } },
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
                        { Localization.getString("enable"), Localization.getString("enable") } },
                getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(P_ENABLE_EXPORT_WITH_SCALING, Localization.getString("pref.commons.enableExportWithScaling"), 2,
                new String[][] { { Localization.getString("disable"), Localization.getString("disable") },
                        { Localization.getString("enable"), Localization.getString("enable") } },
                getFieldEditorParent()));
        addField(
                new RadioGroupFieldEditor(P_ENABLE_EDITING_COMMENT_HISTORY_XML, Localization.getString("pref.commons.enableEditingCommentHistoryXml"),
                        2, new String[][] { { Localization.getString("disable"), Localization.getString("disable") },
                                { Localization.getString("enable"), Localization.getString("enable") } },
                        getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_CONFIRM_DELETION, Localization.getString("pref.commons.confirmDeletion"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_PROCESS_SAVE_HISTORY, Localization.getString("pref.commons.processSaveHistory"), getFieldEditorParent()));
        savepointNumberEditor = new IntegerFieldEditor(P_PROCESS_SAVEPOINT_NUMBER, Localization.getString("pref.commons.processSavepointNumber"),
                getFieldEditorParent(), 2);
        savepointNumberEditor.setValidRange(1, 99);
        savepointNumberEditor.setEnabled(Activator.getPrefBoolean(P_PROCESS_SAVE_HISTORY), getFieldEditorParent());
        addField(savepointNumberEditor);
        enableUserActivityLogging = new BooleanFieldEditor(P_ENABLE_USER_ACTIVITY_LOGGING,
                Localization.getString("pref.commons.enableUserActivityLogging"), getFieldEditorParent());
        addField(enableUserActivityLogging);
        addField(new BooleanFieldEditor(P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED,
                Localization.getString("pref.commons.internalStorageFunctionalityEnabled"), getFieldEditorParent()));
        addField(
                new BooleanFieldEditor(P_GLOBAL_OBJECTS_ENABLED, Localization.getString("pref.commons.enableGlobalObjects"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_CHAT_FUNCTIONALITY_ENABLED, Localization.getString("pref.commons.chatFunctionalityEnabled"),
                getFieldEditorParent()));
        {
            String[][] comboOptions = new String[3][];
            comboOptions[0] = new String[] { Localization.getString("pref.commons." + P_EDITOR_PART_NAME_MODE + "." + P_EDITOR_PART_NAME_MODE_SHORT),
                    P_EDITOR_PART_NAME_MODE_SHORT };
            comboOptions[1] = new String[] {
                    Localization.getString("pref.commons." + P_EDITOR_PART_NAME_MODE + "." + P_EDITOR_PART_NAME_MODE_NON_DUPLICATED),
                    P_EDITOR_PART_NAME_MODE_NON_DUPLICATED };
            comboOptions[2] = new String[] { Localization.getString("pref.commons." + P_EDITOR_PART_NAME_MODE + "." + P_EDITOR_PART_NAME_MODE_LONG),
                    P_EDITOR_PART_NAME_MODE_LONG };
            addField(new ComboFieldEditor(P_EDITOR_PART_NAME_MODE, Localization.getString("pref.commons." + P_EDITOR_PART_NAME_MODE), comboOptions,
                    getFieldEditorParent()));
        }
        addField(new BooleanFieldEditor(P_DISABLE_DOCX_TEMPLATE_VALIDATION, Localization.getString("pref.commons.disableDocxTemplateValidation"),
                getFieldEditorParent()));
    }

    @Override
    protected void performApply() {
        super.performApply();
        UiUtil.updateEditorPartNames(false);
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        if (enableUserActivityLogging.getBooleanValue()) {
            UserActivity.startLogging();
        } else {
            UserActivity.stopLogging();
        }
        UiUtil.updateEditorPartNames(false);
        return result;
    }

    public static boolean isRegulationsMenuItemsEnabled() {
        return Activator.getDefault().getPreferenceStore().getString(P_ENABLE_REGULATIONS_MENU_ITEMS).equals(Localization.getString("enable"));
    }

    public static boolean isExportWithScalingEnabled() {
        return Activator.getDefault().getPreferenceStore().getString(P_ENABLE_EXPORT_WITH_SCALING).equals(Localization.getString("enable"));
    }

    public static boolean isEditingCommentHistoryXmlEnabled() {
        return Activator.getDefault().getPreferenceStore().getString(P_ENABLE_EDITING_COMMENT_HISTORY_XML).equals(Localization.getString("enable"));
    }

    public static boolean isInternalStorageFunctionalityEnabled() {
        return Activator.getDefault().getPreferenceStore().getBoolean(P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED);
    }

    public static boolean isGlobalObjectsEnabled() {
        return Activator.getDefault().getPreferenceStore().getBoolean(P_GLOBAL_OBJECTS_ENABLED);
    }

    public static boolean isChatFunctionalityEnabled() {
        return Activator.getDefault().getPreferenceStore().getBoolean(P_CHAT_FUNCTIONALITY_ENABLED);
    }

    public static String getEditorPartNameMode() {
        String mode = Activator.getDefault().getPreferenceStore().getString(P_EDITOR_PART_NAME_MODE);
        return Strings.isNullOrEmpty(mode) ? P_EDITOR_PART_NAME_MODE_SHORT : mode;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_PROCESS_SAVE_HISTORY.equals(fieldEditor.getPreferenceName())) {
                savepointNumberEditor.setEnabled((Boolean) event.getNewValue(), getFieldEditorParent());
            }
            if (P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED.equals(fieldEditor.getPreferenceName())
                    || P_GLOBAL_OBJECTS_ENABLED.equals(fieldEditor.getPreferenceName())) {
                PluginLogger.logInfoWithDialog(Localization.getString("pref.commons.restart"));
            }
        }
    }

}

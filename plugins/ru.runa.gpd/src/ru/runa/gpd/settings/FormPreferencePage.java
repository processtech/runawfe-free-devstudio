package ru.runa.gpd.settings;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class FormPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {

    private FileFieldEditor formExternalPathEditor;

    public FormPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(P_FORM_USE_EXTERNAL_EDITOR, Localization.getString("pref.form.useExternalEditor"), getFieldEditorParent()));
        formExternalPathEditor = new FileFieldEditor(P_FORM_EXTERNAL_EDITOR_PATH, Localization.getString("pref.form.externalEditorPath"), true,
                getFieldEditorParent());
        boolean enabled = Activator.getPrefBoolean(P_FORM_USE_EXTERNAL_EDITOR);
        formExternalPathEditor.setEnabled(enabled, getFieldEditorParent());
        addField(formExternalPathEditor);
        addField(new RadioGroupFieldEditor(P_FORM_DEFAULT_FCK_EDITOR, Localization.getString("pref.form.defaultFCKEditor"), 2, new String[][] {
                { "FCKEditor 2", FORM_FCK_EDITOR }, { "CKEditor 4", FORM_CK_EDITOR4 } }, getFieldEditorParent()));
        addField(new StringFieldEditor(P_FORM_WEB_SERVER_PORT, Localization.getString("pref.connection.wfe.port"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_FORM_IGNORE_ERRORS_FROM_WEBPAGE, Localization.getString("pref.form.ignoreErrorsFromWebPage"),
                getFieldEditorParent()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (FieldEditor.VALUE.equals(event.getProperty())) {
            FieldEditor fieldEditor = (FieldEditor) event.getSource();
            if (P_FORM_USE_EXTERNAL_EDITOR.equals(fieldEditor.getPreferenceName())) {
                boolean enabled = (Boolean) event.getNewValue();
                formExternalPathEditor.setEnabled(enabled, getFieldEditorParent());
            }
        }
    }

}

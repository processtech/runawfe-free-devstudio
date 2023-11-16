package ru.runa.gpd.formeditor.settings;

import java.util.List;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.parameter.ComboParameter;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String P_DESIGN_PAGE_ENABLED = "designPageEnabled";
    public static final String P_FORM_WEB_SERVER_PORT = "editorWebPort";
    public static final String P_FORM_IGNORE_ERRORS_FROM_WEBPAGE = "ignoreErrorsFromWebPage";
    public static final String P_FORM_DEFAULT_DISPLAY_FORMAT = "defaultDisplayFormat";

    public PreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(P_DESIGN_PAGE_ENABLED, Localization.getString("pref.form.design.page.enabled"), getFieldEditorParent()));
        addField(new IntegerFieldEditor(P_FORM_WEB_SERVER_PORT, Localization.getString("pref.form.port"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_FORM_IGNORE_ERRORS_FROM_WEBPAGE, Localization.getString("pref.form.ignoreErrorsFromWebPage"),
                getFieldEditorParent()));
        addField(new ComboFieldEditor(P_FORM_DEFAULT_DISPLAY_FORMAT, Localization.getString("pref.form.defaultDisplayFormat"), getNamesAndValues(),
                getFieldEditorParent()));
    }

    protected String[][] getNamesAndValues() {
        String[][] result = new String[][] {};
        ComponentType displayVariable = ComponentTypeRegistry.getNotNull("DisplayVariable");
        for (ComponentParameter parameter : displayVariable.getParameters()) {
            if (parameter.getType() instanceof ComboParameter) {
                List<ComboOption> options = parameter.getOptions();
                if (options.size() > 0) {
                    result = new String[options.size()][2];
                    for (int i = 0; i < options.size(); i++) {
                        result[i][0] = options.get(i).getLabel();
                        result[i][1] = options.get(i).getValue();
                    }
                }
            }
        }
        return result;
    }

}

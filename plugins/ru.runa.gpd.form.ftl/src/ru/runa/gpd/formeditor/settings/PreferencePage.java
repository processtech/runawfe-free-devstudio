package ru.runa.gpd.formeditor.settings;

import java.util.List;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.parameter.ComboParameter;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage { // , PrefConstants {

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
        new Label(getFieldEditorParent(), SWT.NONE).setText(Localization.getString("pref.form.defaultDisplayFormat"));
        addField(new ComboFieldEditor(P_FORM_DEFAULT_DISPLAY_FORMAT, "", getNamesAndValues(), getFieldEditorParent()));
    }

    protected String[][] getNamesAndValues() {
        String[][] result = new String[][] {};
        ComponentType displayVariable = ComponentTypeRegistry.getNotNull("DisplayVariable");
        if (displayVariable != null) {
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
        }
        return result;
    }

}

package ru.runa.gpd.settings;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.BotTaskEditor;

public class BotsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    public BotsPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(P_ENABLE_USE_BOT_CONFIG_WITHOUT_PARAMETERS_OPTION,
                Localization.getString("pref.bots.enableUseBotConfigWithoutParametersOption"), getFieldEditorParent()));
        addField(new BooleanFieldEditor(P_SHOW_XML_BOT_CONFIG, Localization.getString("pref.bots.showXmlBotConfig"), getFieldEditorParent()));
    }

    @Override
    public boolean performOk() {
        super.performOk();
        BotTaskEditor.refreshAllBotTaskEditors();
        return true;
    }
}

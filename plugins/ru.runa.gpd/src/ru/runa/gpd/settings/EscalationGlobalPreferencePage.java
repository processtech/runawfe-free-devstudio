package ru.runa.gpd.settings;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;
import ru.runa.wfe.extension.handler.EscalationActionHandler;

import com.google.common.base.Strings;

public class EscalationGlobalPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private final TimerAction timerAction;

    public EscalationGlobalPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        timerAction = new TimerAction(null);
        timerAction.setDelegationClassName(EscalationActionHandler.class.getName());
        String string = Activator.getPrefString(P_ESCALATION_REPEAT);
        if (!Strings.isNullOrEmpty(string)) {
            timerAction.setRepeatDuration(string);
        }
        string = Activator.getPrefString(P_ESCALATION_CONFIG);
        if (!Strings.isNullOrEmpty(string)) {
            timerAction.setDelegationConfiguration(string);
        }
        setDescription(Localization.getString("pref.task.escalation.description"));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new DurationFieldEditor(P_ESCALATION_DURATION, Localization.getString("pref.escalation.duration"), getFieldEditorParent()));
        addField(new ConfigurationFieldEditor(P_ESCALATION_CONFIG, Localization.getString("pref.escalation.actionConfig"), getFieldEditorParent()));
        addField(new DurationFieldEditor(P_ESCALATION_REPEAT, Localization.getString("pref.escalation.repeat"), getFieldEditorParent()));
    }

    private class DurationFieldEditor extends StringButtonFieldEditor {
        private final String initialDuration;

        public DurationFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
            initialDuration = Activator.getPrefString(name);
        }

        @Override
        protected String changePressed() {
            DurationEditDialog dialog = new DurationEditDialog(null, initialDuration);
            Duration duration = (Duration) dialog.openDialog();
            if (duration != null) {
                return duration.getDuration();
            }
            return initialDuration;
        }
    }

    private class ConfigurationFieldEditor extends StringButtonFieldEditor {
        public ConfigurationFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
        }

        @Override
        protected String changePressed() {
            try {
                DelegableProvider provider = HandlerRegistry.getProvider(timerAction.getDelegationClassName());
                String config = provider.showConfigurationDialog(timerAction);
                if (config != null) {
                    timerAction.setDelegationConfiguration(config);
                }
            } catch (Exception ex) {
                PluginLogger.logError("Unable to open configuration dialog for " + timerAction.getDelegationClassName(), ex);
            }
            return timerAction.getDelegationConfiguration();
        }
    }
}

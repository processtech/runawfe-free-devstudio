package ru.runa.gpd.settings;

import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;

import com.google.common.collect.Lists;

public class BPMNPreferencePage extends FieldEditorPreferencePage implements PrefConstants, IWorkbenchPreferencePage {
    public static final String LOCALIZATION_PREFIX = "pref.language.bpmn.";

    public BPMNPreferencePage() {
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
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        applyDialogFont(buttonBar);
    }

    @Override
    public boolean performOk() {
        super.performOk();
        for (LanguageElementPreferencePage page : getCreatedChildPages()) {
            page.performApply();
        }
        LanguageElementPreferenceNode.refreshAllGraphitiEditors();
        return true;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        for (LanguageElementPreferencePage page : getCreatedChildPages()) {
            page.performDefaults();
        }
        try {
            // for inactive pages
            IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("ru.runa.gpd");
            for (String propertyName : preferences.keys()) {
                if (propertyName.startsWith(LanguageElementPreferenceNode.BPMN_ID)) {
                    preferences.remove(propertyName);
                }
            }
        } catch (BackingStoreException e) {
            PluginLogger.logErrorWithoutDialog(e.toString());
        }
    }

    private List<LanguageElementPreferencePage> getCreatedChildPages() {
        List<LanguageElementPreferencePage> result = Lists.newArrayList();
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode languagePreferenceNode = preferenceManager.find(LanguageElementPreferenceNode.ROOT_ID);
        IPreferenceNode bpmnPreferenceNode = languagePreferenceNode.findSubNode(LanguageElementPreferenceNode.BPMN_ID);
        IPreferenceNode[] bpmnElementPreferenceNodes = bpmnPreferenceNode.getSubNodes();
        for (IPreferenceNode node : bpmnElementPreferenceNodes) {
            LanguageElementPreferencePage page = (LanguageElementPreferencePage) node.getPage();
            if (page != null) {
                result.add(page);
                page.performApply();
            }
        }
        return result;
    }
}

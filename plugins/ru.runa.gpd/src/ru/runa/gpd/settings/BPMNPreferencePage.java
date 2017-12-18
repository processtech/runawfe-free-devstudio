package ru.runa.gpd.settings;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ru.runa.gpd.Activator;

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

    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        applyDialogFont(buttonBar);
    }

    @Override
    protected void performApply() {
        setDefaults(false);
    }

    @Override
    protected void performDefaults() {
        setDefaults(true);
    }

    private void setDefaults(boolean apply) {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode[] nodes = pm.getRootSubNodes();
        IPreferenceNode preferenceNodeBPMN = null;
        for (IPreferenceNode preferenceNode : nodes) {
            if (preferenceNode.getId().equals("gpd.pref.language")) {
                preferenceNodeBPMN = preferenceNode.findSubNode(LanguageElementPreferenceNode.BPMN_ID);
                IPreferenceNode[] preferenceNodeBPMNsubNodes = preferenceNodeBPMN.getSubNodes();
                for (IPreferenceNode subNode : preferenceNodeBPMNsubNodes) {
                    LanguageElementPreferencePage page = (LanguageElementPreferencePage) subNode.getPage();
                    if (page == null) {
                        subNode.createPage();
                        page = (LanguageElementPreferencePage) subNode.getPage();
                    }
                    if (!apply) {
                        try {
                            Preferences preferences = InstanceScope.INSTANCE.getNode("ru.runa.gpd");
                            preferences.clear();
                        } catch (BackingStoreException e) {
                        }
                        page.performApply();
                    } else {
                        page.performDefaults();
                    }
                }
                break;
            }
        }
    }
}

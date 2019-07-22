package ru.runa.gpd;

import com.google.common.base.Strings;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.settings.WFEConnectionPreferenceNode;
import ru.runa.gpd.settings.WFEListConnectionsModel;
import ru.runa.gpd.settings.WFEListConnectionsModel.ConItem;
import ru.runa.gpd.settings.WFEListConnectionsPreferenceNode;
import ru.runa.gpd.ui.view.PropertiesView;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor implements PrefConstants {
    private static final String PERSPECTIVE_ID = "ru.runa.gpd.perspective";

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }

    @Override
    public void preStartup() {
        getWorkbenchConfigurer().setSaveAndRestore(true);
        super.preStartup();

        // add Integration Node
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode integrationNode = preferenceManager.find("gpd.pref.connection");
        PreferenceNode connectionsPreferenceNode = new WFEListConnectionsPreferenceNode();
        integrationNode.add(connectionsPreferenceNode);

        // add connection nodes
        int i = ((WFEListConnectionsPreferenceNode) connectionsPreferenceNode).getHead();
        do {
            PreferenceNode node = new WFEConnectionPreferenceNode(i);
            connectionsPreferenceNode.add(node);
            i = ((WFEConnectionPreferenceNode) node).getNext();
            WFEListConnectionsModel.getInstance()
                    .addWFEConnection(new ConItem(((WFEConnectionPreferenceNode) node).getName(), ((WFEConnectionPreferenceNode) node).getId()));
        } while (i != 0);

        // select first default connection (localhost:8080)
        if (Activator.getPrefString(P_WFE_LIST_CONNECTIONS).isEmpty()) {
            Activator.getDefault().getPreferenceStore().setValue(P_WFE_LIST_CONNECTIONS, WFEConnectionPreferenceNode.genId(1));
            ScopedPreferenceStore store = (ScopedPreferenceStore) Activator.getDefault().getPreferenceStore();
            try {
                store.save();
            } catch (IOException e) {
                PluginLogger.logErrorWithoutDialog("Unable save to Preference Store ", e);
            }
        }
    }

    @Override
    public void postStartup() {
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode languagePreferenceNode = preferenceManager.find(LanguageElementPreferenceNode.ROOT_ID);
        IPreferenceNode bpmnPreferenceNode = languagePreferenceNode.findSubNode(LanguageElementPreferenceNode.BPMN_ID);
        IPreferenceNode jpdlPreferenceNode = languagePreferenceNode.findSubNode(LanguageElementPreferenceNode.JPDL_ID);
        for (final NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
            if (definition.isPreferencePageExist()) {
                if (definition.getGraphitiEntry() != null && !Strings.isNullOrEmpty(definition.getBpmnElementName())) {
                    PreferenceNode node = new LanguageElementPreferenceNode(definition, Language.BPMN);
                    bpmnPreferenceNode.add(node);
                }
                if (definition.getGefEntry() != null && !Strings.isNullOrEmpty(definition.getJpdlElementName())) {
                    PreferenceNode node = new LanguageElementPreferenceNode(definition, Language.JPDL);
                    jpdlPreferenceNode.add(node);
                }
            }
        }
        // delete settings page which comes from eclipse
        for (IPreferenceNode preferenceNode : preferenceManager.getRootSubNodes()) {
            if (preferenceNode.getId().contains("gpd") || preferenceNode.getId().contains("equinox.internal.p2")) {
                continue;
            }
            preferenceManager.remove(preferenceNode.getId());
        }
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.ID);
    }

    @Override
    public void postShutdown() {
        try {
            // save the workspace before quit
            ResourcesPlugin.getWorkspace().save(true, null);
        } catch (CoreException e) {
            PluginLogger.logErrorWithoutDialog("Unable to save workspace", e);
        }
        super.postShutdown();
    }

    @Override
    public ContributionComparator getComparatorFor(String contributionType) {
        if (contributionType.equals(IContributionService.TYPE_PREFERENCE)) {
            return new PreferencesComparator();
        } else {
            return super.getComparatorFor(contributionType);
        }
    }

    public static class PreferencesComparator extends ContributionComparator {
        @Override
        public void sort(final Viewer viewer, Object[] elements) {
            if (elements.length > 0) {
                // no sort for WFEConnectionPreferenceNode
                if (!(elements[0] instanceof WFEConnectionPreferenceNode)) {
                    super.sort(viewer, elements);
                }
            } else {
                super.sort(viewer, elements);
            }
        }
    }

}

package ru.runa.gpd;

import com.google.common.base.Strings;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.settings.WfeServerConnectorPreferenceNode;
import ru.runa.gpd.settings.WfeServerConnectorsPreferenceNode;

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
        // support for dynamic connections
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode connectorNode = preferenceManager.find("gpd.pref.connector");
        IPreferenceNode wfeServerConnectorsNode = new WfeServerConnectorsPreferenceNode();
        connectorNode.add(wfeServerConnectorsNode);
        int[] indices = WfeServerConnectorsPreferenceNode.getIndices();
        for (int index : indices) {
            PreferenceNode node = new WfeServerConnectorPreferenceNode(index);
            wfeServerConnectorsNode.add(node);
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
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IContributionItem[] items = ((WorkbenchWindow) workbenchWindow).getMenuBarManager().getItems();
        for (IContributionItem item : items) {
            if (item.getId().equals(IWorkbenchActionConstants.M_WINDOW)) {
                item.setVisible(false);
            }
        }
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

}

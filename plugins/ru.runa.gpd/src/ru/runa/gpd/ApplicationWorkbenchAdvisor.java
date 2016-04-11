package ru.runa.gpd;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.google.common.base.Strings;

import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
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
    }

    @Override
    public void postStartup() {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode[] nodes = pm.getRootSubNodes();
        for (IPreferenceNode preferenceNode : nodes) {
            if (preferenceNode.getId().contains("gpd")) {
                if (preferenceNode.getId().equals("gpd.pref.language")) {
                    IPreferenceNode preferenceNodeBPMN = preferenceNode.findSubNode(LanguageElementPreferenceNode.BPMN_ID);
                    IPreferenceNode preferenceNodeJPDL = preferenceNode.findSubNode(LanguageElementPreferenceNode.JPDL_ID);
                    for (final NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
                        if (NodeTypeDefinition.TYPE_NODE.equals(definition.getType())
                                || NodeTypeDefinition.TYPE_CONNECTION.equals(definition.getType())) {
                            if (definition.getGraphitiEntry() != null && !Strings.isNullOrEmpty(definition.getBpmnElementName())) {
                                PreferenceNode node = new LanguageElementPreferenceNode(definition, Language.BPMN);
                                preferenceNodeBPMN.add(node);
                            }
                            if (definition.getGefEntry() != null && !Strings.isNullOrEmpty(definition.getJpdlElementName())) {
                                PreferenceNode node = new LanguageElementPreferenceNode(definition, Language.JPDL);
                                preferenceNodeJPDL.add(node);
                            }
                        }
                    }
                }
                continue;
            }
            pm.remove(preferenceNode.getId());
        }
    }

    @Override
    public boolean preShutdown() {
        try {
            // save the workspace before quit
            ResourcesPlugin.getWorkspace().save(true, null);
        } catch (CoreException e) {
            PluginLogger.logErrorWithoutDialog("Unable to save workspace", e);
        }
        return super.preShutdown();
    }
}

package ru.runa.gpd.aichat;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.aichat.settings.AiChatLlmConnectorPreferenceNode;
import ru.runa.gpd.aichat.settings.AiChatLlmConnectorsPreferenceNode;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "ru.runa.gpd.aichat"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    public void initializePreferences() {
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode connectorNode = preferenceManager.find("gpd.pref.connector");
        IPreferenceNode aiChatNode = connectorNode.findSubNode("gpd.pref.connector.aichat");
        IPreferenceNode llmNode = new AiChatLlmConnectorsPreferenceNode();
        aiChatNode.add(llmNode);
        int[] llmIndices = AiChatLlmConnectorsPreferenceNode.getIndices();
        for (int index : llmIndices) {
            PreferenceNode node = new AiChatLlmConnectorPreferenceNode(index);
            llmNode.add(node);
        }
    }

    public static String getPrefString(String name) {
        return getDefault().getPreferenceStore().getString(name);
    }

    public static int getPrefInt(String name) {
        return getDefault().getPreferenceStore().getInt(name);
    }

    public static boolean getPrefBoolean(String name) {
        return getDefault().getPreferenceStore().getBoolean(name);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

}

package ru.runa.gpd.aichat.settings;

import com.google.common.base.Joiner;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import ru.runa.gpd.Localization;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.aichat.Messages;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.aichat.sync.AiChatLlmConnectorSettings;

import java.util.ArrayList;
import java.util.List;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_INDICES;
import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_SELECTED_INDEX;

public class AiChatLlmConnectorsPreferenceNode extends PreferenceNode implements PrefConstants {
    public static final String ID = "gpd.pref.connector.aichat.llm";
    private static final String INDICES_DELIMITER = ",";

    public static AiChatLlmConnectorsPreferenceNode getInstance() {
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode connectorNode = preferenceManager.find("gpd.pref.connector");
        IPreferenceNode llmNode = connectorNode.findSubNode("gpd.pref.connector.aichat");
        return (AiChatLlmConnectorsPreferenceNode) llmNode.findSubNode(ID);
    }

    public static int[] getIndices() {
        String indicesString = Activator.getPrefString(P_LLM_CONNECTOR_INDICES);
        String[] strings = indicesString.split(INDICES_DELIMITER, -1);
        int[] result = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = Integer.valueOf(strings[i]);
        }
        return result;
    }

    public static int getSelectedIndex() {
        return Activator.getPrefInt(P_LLM_CONNECTOR_SELECTED_INDEX);
    }

    public static String getSelectedPrefix() {
        return AiChatLlmConnectorPreferenceNode.getId(getSelectedIndex());
    }

    public static String[][] getComboItems() {
        IPreferenceNode[] children = getInstance().getSubNodes();
        String[][] strings = new String[children.length][2];
        for (int i = 0; i < children.length; i++) {
            AiChatLlmConnectorPreferenceNode node = (AiChatLlmConnectorPreferenceNode) children[i];
            strings[i][0] = node.getName();
            strings[i][1] = String.valueOf(node.getIndex());
        }
        return strings;
    }

    public AiChatLlmConnectorsPreferenceNode() {
        super(ID);
    }

    @Override
    public String getLabelText() {
        return Messages.getString("pref.aichat.connection.llm");
    }

    @Override
    public void createPage() {
        setPage(new AiChatLlmConnectorsPreferencePage());
    }

    @Override
    public boolean remove(IPreferenceNode node) {
        boolean removed = super.remove(node);
        if (removed) {
            AiChatLlmConnectorSettings connectorSettings = AiChatLlmConnectorSettings.load(((AiChatLlmConnectorPreferenceNode) node).getIndex());
            connectorSettings.removeFromStore();
            saveIndices();
        }
        return removed;
    }

    @Override
    public IPreferenceNode remove(String id) {
        throw new UnsupportedOperationException("Implement like remove(IPreferenceNode node) if you need it");
    }

    public void saveIndices() {
        IPreferenceNode llmConnectorsNode = AiChatLlmConnectorsPreferenceNode.getInstance();
        IPreferenceNode[] children = llmConnectorsNode.getSubNodes();
        List<String> indices = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            AiChatLlmConnectorPreferenceNode node = (AiChatLlmConnectorPreferenceNode) children[i];
            indices.add(String.valueOf(node.getIndex()));
        }
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setValue(P_LLM_CONNECTOR_INDICES, Joiner.on(INDICES_DELIMITER).join(indices));
    }

    public void updateUi(IPreferencePageContainer preferencePageContainer, String selectedNodeId) {
        if (preferencePageContainer instanceof PreferenceDialog) {
            ((PreferenceDialog) preferencePageContainer).getTreeViewer().refresh();
        }
        if (preferencePageContainer instanceof FilteredPreferenceDialog) {
            ((FilteredPreferenceDialog) preferencePageContainer).setCurrentPageId(selectedNodeId);
        }
    }
}

package ru.runa.gpd.settings;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.WfeServerConnectorSettings;

public class WfeServerConnectorsPreferenceNode extends PreferenceNode implements PrefConstants {
    public static final String ID = "gpd.pref.connector.wfe";
    private static final String INDICES_DELIMITER = ",";

    public static WfeServerConnectorsPreferenceNode getInstance() {
        PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode connectorNode = preferenceManager.find("gpd.pref.connector");
        return (WfeServerConnectorsPreferenceNode) connectorNode.findSubNode(ID);
    }

    public static int[] getIndices() {
        String indicesString = Activator.getPrefString(P_WFE_SERVER_CONNECTOR_INDICES);
        String[] strings = indicesString.split(INDICES_DELIMITER, -1);
        int[] result = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = Integer.valueOf(strings[i]);
        }
        return result;
    }

    public static int getSelectedIndex() {
        return Activator.getPrefInt(P_WFE_SERVER_CONNECTOR_SELECTED_INDEX);
    }

    public static String getSelectedPrefix() {
        return WfeServerConnectorPreferenceNode.getId(getSelectedIndex());
    }

    public static String[][] getComboItems() {
        IPreferenceNode[] children = getInstance().getSubNodes();
        String[][] strings = new String[children.length][2];
        for (int i = 0; i < children.length; i++) {
            WfeServerConnectorPreferenceNode node = (WfeServerConnectorPreferenceNode) children[i];
            strings[i][0] = node.getName();
            strings[i][1] = String.valueOf(node.getIndex());
        }
        return strings;
    }

    public WfeServerConnectorsPreferenceNode() {
        super(ID);
    }

    @Override
    public String getLabelText() {
        return Localization.getString("pref.connection.wfe");
    }

    @Override
    public void createPage() {
        setPage(new WfeServerConnectorsPreferencePage());
    }

    @Override
    public boolean remove(IPreferenceNode node) {
        boolean removed = super.remove(node);
        if (removed) {
            WfeServerConnectorSettings connectorSettings = WfeServerConnectorSettings.load(((WfeServerConnectorPreferenceNode) node).getIndex());
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
        IPreferenceNode wfeServerConnectorsNode = WfeServerConnectorsPreferenceNode.getInstance();
        IPreferenceNode[] children = wfeServerConnectorsNode.getSubNodes();
        List<String> indices = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            WfeServerConnectorPreferenceNode node = (WfeServerConnectorPreferenceNode) children[i];
            indices.add(String.valueOf(node.getIndex()));
        }
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setValue(P_WFE_SERVER_CONNECTOR_INDICES, Joiner.on(INDICES_DELIMITER).join(indices));
    }

    public void updateUi(IPreferencePageContainer preferencePageContainer, String selectedNodeId) {
        if (preferencePageContainer instanceof PreferenceDialog) {
            ((PreferenceDialog) preferencePageContainer).getTreeViewer().refresh();
        }
        if (preferencePageContainer instanceof FilteredPreferenceDialog) {
            // #624#note-15
            ((FilteredPreferenceDialog) preferencePageContainer).setCurrentPageId(selectedNodeId);
        }
    }
}

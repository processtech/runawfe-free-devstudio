package ru.runa.gpd.settings;

import org.eclipse.jface.preference.PreferenceNode;
import ru.runa.gpd.Activator;

public class WfeServerConnectorPreferenceNode extends PreferenceNode implements PrefConstants {
    private final int index;
    private String name;

    public static String getId(int index) {
        return WfeServerConnectorsPreferenceNode.ID + "." + index;
    }

    public WfeServerConnectorPreferenceNode(int index) {
        super(getId(index));
        this.index = index;
        updateName();
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void updateName() {
        String host = Activator.getPrefString(this.getId() + "." + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX);
        this.name = host.isEmpty() ? "[new connection]" : host;
    }

    public boolean isSelected() {
        return index == WfeServerConnectorsPreferenceNode.getSelectedIndex();
    }

    @Override
    public String getLabelText() {
        return this.name;
    }

    @Override
    public void createPage() {
        setPage(new WfeServerConnectorPreferencePage(getId()));
    }
}

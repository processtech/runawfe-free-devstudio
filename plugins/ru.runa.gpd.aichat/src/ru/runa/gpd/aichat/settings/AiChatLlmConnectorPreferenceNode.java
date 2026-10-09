package ru.runa.gpd.aichat.settings;

import org.eclipse.jface.preference.PreferenceNode;
import ru.runa.gpd.aichat.Activator;
import ru.runa.gpd.settings.PrefConstants;

import static ru.runa.gpd.aichat.settings.PrefConstants.P_LLM_CONNECTOR_HOST_SUFFIX;

public class AiChatLlmConnectorPreferenceNode extends PreferenceNode implements PrefConstants {
    private final int index;
    private String name;

    public static String getId(int index) {
        return AiChatLlmConnectorsPreferenceNode.ID + "." + index;
    }

    public AiChatLlmConnectorPreferenceNode(int index) {
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
        String host = Activator.getPrefString(this.getId() + "." + P_LLM_CONNECTOR_HOST_SUFFIX);
        this.name = host.isEmpty() ? "[new connection]" : host;
    }

    public boolean isSelected() {
        return index == AiChatLlmConnectorsPreferenceNode.getSelectedIndex();
    }

    @Override
    public String getLabelText() {
        return this.name;
    }

    @Override
    public void createPage() {
        setPage(new AiChatLlmConnectorPreferencePage(getId()));
    }
}

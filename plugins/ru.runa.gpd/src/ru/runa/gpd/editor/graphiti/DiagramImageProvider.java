package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.platform.AbstractExtension;
import org.eclipse.graphiti.ui.platform.IImageProvider;

public class DiagramImageProvider extends AbstractExtension implements IImageProvider {
    private String pluginId;

    @Override
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public String getImageFilePath(String imageId) {
        return "icons/bpmn/" + imageId;
    }
}

package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.regulations.NodeRegulationsProperties;
import ru.runa.gpd.lang.model.Node;

public class ChangeRegulationsPropertiesFeature extends ChangePropertyFeature<Node, NodeRegulationsProperties> {

    public ChangeRegulationsPropertiesFeature(Node target, NodeRegulationsProperties newValue) {
        super(target, target.getRegulationsProperties(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setRegulationsProperties(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setRegulationsProperties(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("feature.change.regulations.properties");
    }

}

package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;

public class DiagramTypeProvider extends AbstractDiagramTypeProvider {
    private IToolBehaviorProvider[] toolBehaviorProviders;

    public DiagramTypeProvider() {
        super();
        setFeatureProvider(new DiagramFeatureProvider(this));
    }

    @Override
    public IToolBehaviorProvider[] getAvailableToolBehaviorProviders() {
        if (toolBehaviorProviders == null) {
            toolBehaviorProviders = new IToolBehaviorProvider[] { new DiagramToolBehaviorProvider(this) };
        }
        return toolBehaviorProviders;
    }
}

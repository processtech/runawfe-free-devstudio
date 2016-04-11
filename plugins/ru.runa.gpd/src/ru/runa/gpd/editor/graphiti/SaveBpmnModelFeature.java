package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

public class SaveBpmnModelFeature extends AbstractCustomFeature {
    public SaveBpmnModelFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public String getName() {
        return "Save to bpmn 2.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "Generate the bpmn 2.0 xml file"; //$NON-NLS-1$
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return true;
    }

    @Override
    public void execute(ICustomContext context) {
        System.out.println("CUSTOM FEATURE EXECUTED");
    }
}

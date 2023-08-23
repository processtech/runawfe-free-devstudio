package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

import ru.runa.gpd.lang.model.bpmn.CycleType;

public class ChangeCycleTypeFeature extends AbstractCustomFeature {
	
	private CycleType newType;
	
	public ChangeCycleTypeFeature(IFeatureProvider fp, CycleType newType) {
        super(fp);
        this.newType = newType;
    }

	@Override
	public void execute(ICustomContext context) {
		// TODO Auto-generated method stub
		
	}
}

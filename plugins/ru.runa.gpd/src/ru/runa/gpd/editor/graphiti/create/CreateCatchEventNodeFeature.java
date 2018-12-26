package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;

public class CreateCatchEventNodeFeature extends CreateElementFeature {

    @Override
    public boolean canCreate(ICreateContext context) {
        if (super.canCreate(context)) {
            return true;
        }
        
        GraphElement container = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        GraphElement containerParent = container.getParent();
        return container instanceof IBoundaryEventContainer && !(containerParent instanceof IBoundaryEventContainer)
                && container.getChildren(CatchEventNode.class).isEmpty();
    }

}

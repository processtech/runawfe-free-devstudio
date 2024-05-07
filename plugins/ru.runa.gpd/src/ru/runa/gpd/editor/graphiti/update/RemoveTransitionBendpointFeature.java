package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
import org.eclipse.graphiti.features.impl.DefaultRemoveBendpointFeature;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.AbstractTransition;

public class RemoveTransitionBendpointFeature extends DefaultRemoveBendpointFeature implements CustomUndoRedoFeature {

    private Point undoBendpoint;
    private boolean allowRedo = false;

    public RemoveTransitionBendpointFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void removeBendpoint(IRemoveBendpointContext context) {
        super.removeBendpoint(context);
        int index = context.getBendpointIndex();
        FreeFormConnection connection = context.getConnection();
        AbstractTransition transition = (AbstractTransition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
        undoBendpoint = transition.getBendpoints().get(index).getCopy();
        transition.removeBendpoint(index);
    }

    @Override
    public boolean canUndo(IContext context) {
        return undoBendpoint != null;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IRemoveBendpointContext) {
            int index = ((IRemoveBendpointContext) context).getBendpointIndex();
            FreeFormConnection connection = ((IRemoveBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            transition.addBendpoint(index, undoBendpoint);
            allowRedo = true;
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return allowRedo;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IRemoveBendpointContext) {
            FreeFormConnection connection = ((IRemoveBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            transition.removeBendpoint(((IRemoveBendpointContext) context).getBendpointIndex());
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}

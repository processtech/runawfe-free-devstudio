package ru.runa.gpd.editor.gef.part.graph;

import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoutingListener;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;

import ru.runa.gpd.editor.gef.ActionGraphUtils;
import ru.runa.gpd.editor.gef.policy.ActionComponentEditPolicy;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class ActionGraphicalEditPart extends ElementGraphicalEditPart {
    //    @Override
    //    public void propertyChange(PropertyChangeEvent evt) {
    //        if (.equals(evt.getPropertyName()) || PROPERTY_CONFIGURATION.equals(evt.getPropertyName())) {
    //            updateTooltip(getFigure());
    //            refreshVisuals();
    //        }
    //    }
    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_CLASS);
        list.add(PROPERTY_CONFIGURATION);
    }

    @Override
    public Action getModel() {
        return (Action) super.getModel();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ActionComponentEditPolicy());
    }

    @Override
    protected IFigure createFigure() {
        IFigure figure = super.createFigure();
        if (getParent() instanceof TransitionGraphicalEditPart) {
            PolylineConnection connection = (PolylineConnection) ((TransitionGraphicalEditPart) getParent()).getConnectionFigure();
            connection.addRoutingListener(new RoutingListener() {
                @Override
                public void invalidate(Connection connection) {
                }

                @Override
                public void postRoute(Connection connection) {
                    if (getParent() == null) {
                        return;
                    }
                    int index = ((ActionContainer) getParent().getModel()).getActions().indexOf(getModel());
                    getFigure().setLocation(ActionGraphUtils.getActionFigureLocation(((TransitionGraphicalEditPart) getParent()).getConnectionFigure(), index, 0, false));
                    refreshVisuals();
                }

                @Override
                public void remove(Connection connection) {
                }

                @Override
                public boolean route(Connection connection) {
                    return false;
                }

                @Override
                public void setConstraint(Connection connection, Object constraint) {
                }
            });
        }
        return figure;
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        return new WithConnectionLayerDragEditPartsTracker(this);
    }

    static class WithConnectionLayerDragEditPartsTracker extends DragEditPartsTracker {
        public WithConnectionLayerDragEditPartsTracker(EditPart sourceEditPart) {
            super(sourceEditPart);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Collection<? extends EditPart> getExclusionSet() {
            return getOperationSet();
        }
    }
}

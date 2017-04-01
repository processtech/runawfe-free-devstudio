package ru.runa.gpd.editor.graphiti.update;

import java.util.List;

import org.eclipse.graphiti.features.ICustomUndoableFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;
import ru.runa.gpd.lang.model.jpdl.Action;

public class DeleteElementFeature extends DefaultDeleteFeature implements ICustomUndoableFeature {

    private GraphElement element;
    private List<Transition> leavingTransitions;
    private List<Transition> arrivingTransitions;

    public DeleteElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    protected boolean getUserDecision(IDeleteContext context) {
        return true;
    }

    @Override
    protected boolean getUserDecision() {
        return true;
    }

    @Override
    protected void deleteBusinessObject(Object bo) {
        if (bo == null)
            return;
        element = (GraphElement) bo;
        if (element instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) element;
            textDecoration.getTarget().getParent().removeChild(textDecoration.getTarget());
            removeAndStoreTransitions(textDecoration.getTarget());
        } else if (element instanceof HasTextDecorator) {
            HasTextDecorator withDefinition = (HasTextDecorator) element;
            IDeleteContext delContext = new DeleteContext(withDefinition.getTextDecoratorEmulation().getDefinition().getUiContainer().getOwner());
            delete(delContext);
        } else if (element instanceof Node) {
            Node node = (Node) element;
            removeAndStoreTransitions(node);
        } else if (element instanceof Transition) {
            Transition transition = (Transition) element;
            transition.getSource().removeLeavingTransition(transition);
            return;
        }
        element.getParent().removeChild(element);
    }

    @Override
    public boolean canUndo(IContext context) {
        return element != null;
    }

    @Override
    public void undo(IContext context) {
        if (element instanceof Transition) {
            Transition transition = (Transition) element;
            transition.getSource().addChild(transition);
            return;
        }

        if (element instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) element;
            textDecoration.getTarget().getParent().addChild(textDecoration.getTarget());
            restoreTransitions();
        } else {
            element.getParent().addChild(element);
        }
        if (element instanceof Node)
            restoreTransitions();
    }

    @Override
    public boolean canRedo(IContext context) {
        return element != null;
    }

    @Override
    public void redo(IContext context) {
        deleteBusinessObject(element);
    }

    private void removeAndStoreTransitions(Node node) {
        leavingTransitions = node.getLeavingTransitions();
        for (Transition transition : leavingTransitions) {
            transition.getSource().removeLeavingTransition(transition);
        }
        arrivingTransitions = node.getArrivingTransitions();
        for (Transition transition : arrivingTransitions) {
            transition.getSource().removeLeavingTransition(transition);
        }
    }

    private void restoreTransitions() {
        if (leavingTransitions != null) {
            for (Transition transition : leavingTransitions) {
                transition.getSource().addChild(transition);
            }
        }
        if (arrivingTransitions != null) {
            for (Transition transition : arrivingTransitions) {
                transition.getSource().addChild(transition);
            }
        }
    }

    @Override
    public void preDelete(IDeleteContext context) {
        super.preDelete(context);
        if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof Action) {
            PictogramElement pe = context.getPictogramElement();
            context.putProperty(
                    "action-container",
                    pe instanceof ConnectionDecorator ?
                            ((ConnectionDecorator) context.getPictogramElement()).getConnection() :
                                ((Shape) context.getPictogramElement()).getContainer());
        }
    }

    @Override
    public void postDelete(IDeleteContext context) {
        super.postDelete(context);
        if (element instanceof Action) {
            layoutPictogramElement((PictogramElement) context.getProperty("action-container"));
        }
    }

}

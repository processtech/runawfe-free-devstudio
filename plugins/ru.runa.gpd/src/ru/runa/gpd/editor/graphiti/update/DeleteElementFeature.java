package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Strings;
import java.text.MessageFormat;
import java.util.List;
import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IMultiDeleteInfo;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;
import ru.runa.gpd.settings.PrefConstants;

public class DeleteElementFeature extends DefaultDeleteFeature implements ICustomUndoRedoFeature {

    private static final String NAME = Localization.getString("DeleteElementFeature_1");

    private GraphElement element;
    private List<Transition> leavingTransitions;
    private List<Transition> arrivingTransitions;

    public DeleteElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    // based on default implementation: DefaultDeleteFeature#getUserDecision(IDeleteContext)
    protected boolean getUserDecision(IDeleteContext context) {
        if (!Activator.getPrefBoolean(PrefConstants.P_CONFIRM_DELETION)) {
            return true;
        }
        String msg;
        IMultiDeleteInfo multiDeleteInfo = context.getMultiDeleteInfo();
        if (multiDeleteInfo != null) {
            msg = MessageFormat.format(Localization.getString("DeleteElementFeature_2"), multiDeleteInfo.getNumber());
        } else {
            if (((GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement())) instanceof TextDecorationNode) {
                return true;
            }
            String deleteName = getDeleteName(context);
            if (deleteName != null && deleteName.length() > 0) {
                msg = MessageFormat.format(Localization.getString("DeleteElementFeature_3"), deleteName);
            } else {
                msg = Localization.getString("DeleteElementFeature_4");
            }
        }
        return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                Localization.getString("DeleteElementFeature_5"), msg);
    }

    @Override
    protected String getDeleteName(IDeleteContext context) {
        GraphElement ge = ((GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement()));
        if (ge instanceof NamedGraphElement) {
            String name = ((NamedGraphElement) ge).getName();
            if (!Strings.isNullOrEmpty(name)) {
                return name;
            }
        }
        return ge.getId();
    }

    @Override
    protected void deleteBusinessObject(Object bo) {
        if (bo == null) {
            return;
        }
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
    public void postUndo(IContext context) {
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
        if (element instanceof Node) {
            restoreTransitions();
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return element != null;
    }

    @Override
    public void postRedo(IContext context) {
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

    @Override
    public void preUndo(IContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void preRedo(IContext context) {
        // TODO Auto-generated method stub

    }

}

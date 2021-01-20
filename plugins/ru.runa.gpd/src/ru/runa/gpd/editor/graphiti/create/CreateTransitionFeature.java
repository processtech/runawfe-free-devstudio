package ru.runa.gpd.editor.graphiti.create;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.internal.util.ui.PopupMenu;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

public class CreateTransitionFeature extends AbstractCreateConnectionFeature {
    private final NodeTypeDefinition transitionDefinition;
    private IFeatureProvider featureProvider;

    public CreateTransitionFeature() {
        super(null, "", "");
        this.transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
    }

    public void setFeatureProvider(IFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public String getCreateName() {
        return transitionDefinition.getLabel();
    }

    @Override
    public String getCreateImageId() {
        return transitionDefinition.getPaletteIcon();
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        if (source instanceof Node) {
            Node sourceNode = (Node) source;
            return sourceNode.canAddLeavingTransition();
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        Object target = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (target instanceof Node) {
            return ((Node) target).canAddArrivingTransition((Node) source);
        } else if (target instanceof ProcessDefinition) {
            return true;
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Object tpe = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (tpe instanceof ProcessDefinition) {
            List<NodeTypeDefinition> definitions = Lists.newArrayList();
            for (ICreateFeature feature : getFeatureProvider().getCreateFeatures()) {
                if (feature instanceof CreateSwimlaneFeature || feature instanceof CreateStartNodeFeature || feature instanceof CreateDataStoreFeature
                        || feature instanceof CreateAnnotationFeature) {
                    continue;
                }
                if (feature instanceof CreateElementFeature) {
                    definitions.add(((CreateElementFeature) feature).getNodeDefinition());
                }
            }
            PopupMenu menu = new PopupMenu(definitions, new LabelProvider() {

                @Override
                public Image getImage(Object element) {
                    return ((NodeTypeDefinition) element).getImage(Language.BPMN.getNotation());
                }

                @Override
                public String getText(Object element) {
                    return ((NodeTypeDefinition) element).getLabel();
                }
            });
            if (menu.show(Display.getCurrent().getActiveShell())) {
                CreateConnectionContext createConnectionContext = new CreateConnectionContext();
                createConnectionContext.setSourcePictogramElement(context.getSourcePictogramElement());
                createConnectionContext.setTargetLocation(context.getTargetLocation());
                ContainerShape targetContainer = getFeatureProvider().getDiagramTypeProvider().getDiagram();
                CreateContext createContext = new CreateContext();
                createContext.setTargetContainer(targetContainer);
                createContext.putProperty(CreateElementFeature.CONNECTION_PROPERTY, createConnectionContext);
                CreateDragAndDropElementFeature createDragAndDropElementFeature = new CreateDragAndDropElementFeature(createContext);
                createDragAndDropElementFeature.setNodeDefinition((NodeTypeDefinition) menu.getResult());
                createDragAndDropElementFeature.setFeatureProvider((DiagramFeatureProvider) getFeatureProvider());
                createDragAndDropElementFeature.create(createConnectionContext);
            }
            return null;
        } else { // Node
            Node source = (Node) getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
            // create new business object
            Transition transition = transitionDefinition.createElement(source, false);
            transition.setTarget((Node) tpe);
            transition.setName(source.getNextTransitionName(transitionDefinition));
            source.addLeavingTransition(transition);
            // add connection for business object
            Anchor sourceAnchor = context.getSourceAnchor();
            if (sourceAnchor == null) {
                sourceAnchor = getChopboxAnchor(context.getSourcePictogramElement());
            }
            Anchor targetAnchor = context.getTargetAnchor();
            if (targetAnchor == null) {
                targetAnchor = getChopboxAnchor(context.getTargetPictogramElement());
            }
            AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
            addConnectionContext.setNewObject(transition);
            return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
        }
    }

    private Anchor getChopboxAnchor(PictogramElement pe) {
        if (pe instanceof AnchorContainer) {
            Anchor anchor = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
            if (anchor != null) {
                return anchor;
            }
        }
        if (pe instanceof ContainerShape) {
            for (Shape shape : ((ContainerShape) pe).getChildren()) {
                Anchor anchor = getChopboxAnchor(shape);
                if (anchor != null) {
                    return anchor;
                }
            }
        }
        return null;
    }

}

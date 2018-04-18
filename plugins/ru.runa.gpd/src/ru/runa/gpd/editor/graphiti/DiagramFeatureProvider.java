package ru.runa.gpd.editor.graphiti;

import java.util.List;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddBendpointFeature;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IMoveBendpointFeature;
import org.eclipse.graphiti.features.IMoveConnectionDecoratorFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.IRemoveBendpointFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddBendpointContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IMoveBendpointContext;
import org.eclipse.graphiti.features.context.IMoveConnectionDecoratorContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.graphiti.add.AddTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.BOUpdateContext;
import ru.runa.gpd.editor.graphiti.update.DeleteElementFeature;
import ru.runa.gpd.editor.graphiti.update.DirectEditDescriptionFeature;
import ru.runa.gpd.editor.graphiti.update.DirectEditNodeNameFeature;
import ru.runa.gpd.editor.graphiti.update.MoveElementFeature;
import ru.runa.gpd.editor.graphiti.update.MoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.MoveTransitionLabelFeature;
import ru.runa.gpd.editor.graphiti.update.ReconnectSequenceFlowFeature;
import ru.runa.gpd.editor.graphiti.update.RemoveTransitionBendpointFeature;
import ru.runa.gpd.editor.graphiti.update.ResizeElementFeature;
import ru.runa.gpd.lang.BpmnSerializer;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.bpmn.TextAnnotation;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;

@SuppressWarnings("unchecked")
public class DiagramFeatureProvider extends DefaultFeatureProvider {

    public DiagramFeatureProvider(IDiagramTypeProvider dtp) {
        super(dtp);
        setIndependenceSolver(new IndependenceSolver());
    }

    public ProcessDefinition getCurrentProcessDefinition() {
        return ProcessCache.getProcessDefinition(IOUtils.getCurrentFile());
    }

    @Override
    public ICreateFeature[] getCreateFeatures() {
        ProcessDefinition processDefinition = ((DiagramEditorPage) getDiagramTypeProvider().getDiagramBehavior().getDiagramContainer())
                .getDefinition();
        List<ICreateFeature> list = Lists.newArrayList();
        for (NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
            if (definition.getGraphitiEntry() != null && !Strings.isNullOrEmpty(definition.getBpmnElementName())) {
                if (NodeTypeDefinition.TYPE_NODE.equals(definition.getType())) {
                    if ("endEvent".equals(definition.getBpmnElementName())) {
                        if (processDefinition instanceof SubprocessDefinition) {
                            continue;
                        }
                    }
                    list.add((ICreateFeature) definition.getGraphitiEntry().createCreateFeature(this));
                }
                if (NodeTypeDefinition.TYPE_ARTIFACT.equals(definition.getType())) {
                    if ("lane".equals(definition.getBpmnElementName())) {
                        if (SwimlaneDisplayMode.none == processDefinition.getSwimlaneDisplayMode()) {
                            continue;
                        }
                    }
                    if ("actionHandler".equals(definition.getBpmnElementName()) && !processDefinition.isShowActions()) {
                        continue;
                    }
                    if (BpmnSerializer.START_TEXT_DECORATION.equals(definition.getBpmnElementName())) {
                        continue;
                    }
                    if (BpmnSerializer.END_TEXT_DECORATION.equals(definition.getBpmnElementName())) {
                        continue;
                    }
                    list.add((ICreateFeature) definition.getGraphitiEntry().createCreateFeature(this));
                }
            }
        }
        return list.toArray(new ICreateFeature[list.size()]);
    }

    public IAddFeature getAddFeature(Class<? extends GraphElement> nodeClass) {
        NodeTypeDefinition definition = NodeRegistry.getNodeTypeDefinition(nodeClass);
        if (definition != null && definition.getGraphitiEntry() != null) {
            return definition.getGraphitiEntry().createAddFeature(this);
        }
        return null;
    }

    @Override
    public IAddFeature getAddFeature(IAddContext context) {
        return getAddFeature((Class<? extends GraphElement>) context.getNewObject().getClass());
    }

    @Override
    public ILayoutFeature getLayoutFeature(ILayoutContext context) {
        GraphElement bo = (GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (bo == null) {
            return null;
        }
        return bo.getTypeDefinition().getGraphitiEntry().createLayoutFeature(this);
    }

    @Override
    public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
        return new MoveElementFeature(this);
    }

    @Override
    public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
        GraphElement bo = (GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (bo == null) {
            return null;
        }
        return new ResizeElementFeature(this);
    }

    @Override
    public IDeleteFeature getDeleteFeature(IDeleteContext context) {
        return new DeleteElementFeature(this);
    }

    @Override
    public ICreateConnectionFeature[] getCreateConnectionFeatures() {
        List<ICreateConnectionFeature> list = Lists.newArrayList();
        for (NodeTypeDefinition definition : NodeRegistry.getDefinitions()) {
            if (definition.getGraphitiEntry() != null && NodeTypeDefinition.TYPE_CONNECTION.equals(definition.getType())) {
                list.add((ICreateConnectionFeature) definition.getGraphitiEntry().createCreateFeature(this));
            }
        }
        return list.toArray(new ICreateConnectionFeature[list.size()]);
    }

    @Override
    public IAddBendpointFeature getAddBendpointFeature(IAddBendpointContext context) {
        return new AddTransitionBendpointFeature(this);
    }

    @Override
    public IMoveConnectionDecoratorFeature getMoveConnectionDecoratorFeature(IMoveConnectionDecoratorContext context) {
        return new MoveTransitionLabelFeature(this);
    }

    @Override
    public IMoveBendpointFeature getMoveBendpointFeature(IMoveBendpointContext context) {
        return new MoveTransitionBendpointFeature(this);
    }

    @Override
    public IRemoveBendpointFeature getRemoveBendpointFeature(IRemoveBendpointContext context) {
        return new RemoveTransitionBendpointFeature(this);
    }

    @Override
    public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
        return new ReconnectSequenceFlowFeature(this);
    }

    @Override
    public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object bo = getBusinessObjectForPictogramElement(pe);
        if (bo instanceof Node) {
            return new DirectEditNodeNameFeature(this);
        }
        if (bo instanceof TextAnnotation) {
            return new DirectEditDescriptionFeature(this);
        }
        return null;
    }

    @Override
    public IUpdateFeature getUpdateFeature(IUpdateContext context) {
        Object bo;
        if (context instanceof BOUpdateContext) {
            bo = ((BOUpdateContext) context).getModel();
        } else {
            PictogramElement pictogramElement = context.getPictogramElement();
            bo = getBusinessObjectForPictogramElement(pictogramElement);
        }
        if (bo instanceof GraphElement) {
            NodeTypeDefinition definition = ((GraphElement) bo).getTypeDefinition();
            if (definition != null && definition.getGraphitiEntry() != null) {
                return definition.getGraphitiEntry().createUpdateFeature(this);
            }
        }
        return super.getUpdateFeature(context);
    }

}

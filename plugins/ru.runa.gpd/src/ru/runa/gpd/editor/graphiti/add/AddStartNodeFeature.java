package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.BpmnSerializer;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.bpmn.StartTextDecoration;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class AddStartNodeFeature extends AddNodeWithImageFeature {
    @Override
    public PictogramElement add(IAddContext context) {
        PictogramElement containerShape = super.add(context);
        StartState node = (StartState) context.getNewObject();
        Dimension bounds = getBounds(context);

        // create independent text label graph element for start point
        StartTextDecoration element = NodeRegistry.getNodeTypeDefinition(Language.BPMN, BpmnSerializer.START_TEXT_DECORATION).createElement(node.getParent(),
                false);

        node.getTextDecoratorEmulation().link(element);
        AreaContext tempArea = new AreaContext();

        if (node.getTextDecoratorEmulation().hasDefinitionLocation()) {
            // get saved position
            tempArea.setLocation(node.getTextDecoratorEmulation().getDefinitionLocation().x(), node.getTextDecoratorEmulation().getDefinitionLocation().y());
        } else {
            // calc position under start point image
            Font defFont = Graphiti.getGaService().manageDefaultFont(getDiagram());
            
            IDimension swimlaneDim = new DimensionImpl(0, 0);
            if (SwimlaneDisplayMode.none == node.getProcessDefinition().getSwimlaneDisplayMode() ) {
            	swimlaneDim = GraphitiUi.getUiLayoutService().calculateTextSize(node.getSwimlaneLabel(), defFont);
        	}
            
            IDimension nameDim = GraphitiUi.getUiLayoutService().calculateTextSize(node.getName(), defFont);
            int y = context.getY() - swimlaneDim.getHeight() - nameDim.getHeight();
            int maxWidth = Math.max(swimlaneDim.getWidth(), nameDim.getWidth());
            int x = (context.getX() + bounds.width / 2) - maxWidth / 2;
            tempArea.setLocation(x, y);
        }

        // put new element in run-time
        AddContext myAddContext = new AddContext(tempArea, element);
        myAddContext.setTargetContainer(context.getTargetContainer());
        AddStartTextDecorationFeature textDefinition = new AddStartTextDecorationFeature();
        textDefinition.setFeatureProvider((DiagramFeatureProvider) getFeatureProvider());
        textDefinition.add(myAddContext);

        return containerShape;
    }
}

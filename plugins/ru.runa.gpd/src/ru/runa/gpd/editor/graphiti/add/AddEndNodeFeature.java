package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.BpmnSerializer;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.AbstractEndTextDecorated;
import ru.runa.gpd.lang.model.EndTextDecoration;

public class AddEndNodeFeature extends AddNodeWithImageFeature {

    @Override
    public PictogramElement add(IAddContext context) {
        PictogramElement container = super.add(context);
        AbstractEndTextDecorated node = (AbstractEndTextDecorated) context.getNewObject();
        Dimension bounds = adjustBounds(context);

        // create independent text label graph element for end point
        EndTextDecoration element = NodeRegistry.getNodeTypeDefinition(Language.BPMN, BpmnSerializer.END_TEXT_DECORATION).createElement(node.getParent(),
                false);

        node.getTextDecoratorEmulation().link(element);
        AreaContext tempArea = new AreaContext();

        if (node.getTextDecoratorEmulation().hasDefinitionLocation()) {
            // get saved position
            tempArea.setLocation(node.getTextDecoratorEmulation().getDefinitionLocation().x(), node.getTextDecoratorEmulation().getDefinitionLocation().y());
        } else {
            // calc position under end point image
            Font defFont = Graphiti.getGaService().manageDefaultFont(getDiagram());
            IDimension nameDim = GraphitiUi.getUiLayoutService().calculateTextSize(node.getName(), defFont);
            int y = context.getY() + bounds.height;
            int x = (context.getX() + bounds.width / 2) - nameDim.getWidth() / 2;
            tempArea.setLocation(x, y);
        }

        // put new element in run-time
        AddContext myAddContext = new AddContext(tempArea, element);
        myAddContext.setTargetContainer(context.getTargetContainer());
        AddEndTextDecorationFeature textDefinition = new AddEndTextDecorationFeature();
        textDefinition.setFeatureProvider((DiagramFeatureProvider) getFeatureProvider());
        textDefinition.add(myAddContext);

        return container;
    }

}

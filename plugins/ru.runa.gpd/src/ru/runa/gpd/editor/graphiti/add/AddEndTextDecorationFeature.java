package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.lang.model.bpmn.EndTextDecoration;

public class AddEndTextDecorationFeature extends AddNodeFeature {

    @Override
    public PictogramElement add(IAddContext context) {
        EndTextDecoration node = (EndTextDecoration) context.getNewObject();
        String labelName = node.getTarget().getName();

        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);

        adjustBounds(context);
        IGaService gaService = Graphiti.getGaService();

        Text textName = gaService.createDefaultText(getDiagram(), containerShape, labelName);
        gaService.setLocation(textName, context.getX(), context.getY());
        node.setUiContainer(node.new EndDefinitionUI(containerShape, textName));

        link(containerShape, node);
        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);

        return containerShape;

    }

}

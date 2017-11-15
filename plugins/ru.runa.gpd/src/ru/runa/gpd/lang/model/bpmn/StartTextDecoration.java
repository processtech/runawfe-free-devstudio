package ru.runa.gpd.lang.model.bpmn;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.editor.graphiti.UIContainer;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class StartTextDecoration extends TextDecorationNode {

    @Override
    public StartState getTarget() {
        return (StartState) target;
    }

    public class StartDefinitionUI implements UIContainer {

        private final PictogramElement owner;
        private final Rectangle box;
        private final Text name;
        private final Text swimlane;

        public StartDefinitionUI(PictogramElement owner, Rectangle box, Text name, Text swimlane) {
            this.owner = owner;
            this.box = box;
            this.name = name;
            this.swimlane = swimlane;
            pack();
        }

        @Override
        public void pack() {
            IGaService gaService = Graphiti.getGaService();
            StartState node = (StartState) target;
            String labelName = node.getName();
            String labelSwimline = new String();
            if (node.getSwimlaneLabel() != null) {
                labelSwimline = node.getSwimlaneLabel();
            }

            int maxRectWidth = 0;
            IDimension swimlineDim = new DimensionImpl(0, 0);

            if (SwimlaneDisplayMode.none == target.getProcessDefinition().getSwimlaneDisplayMode()) {
            	swimlineDim = GraphitiUi.getUiLayoutService().calculateTextSize(labelSwimline, getFont(swimlane));
            	maxRectWidth = swimlineDim.getWidth();
                swimlane.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
            }

            IDimension nameDim = GraphitiUi.getUiLayoutService().calculateTextSize(labelName, getFont(name));            
            maxRectWidth = Math.max(nameDim.getWidth(), maxRectWidth);
            name.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
            gaService.setLocationAndSize(name, 0, swimlineDim.getHeight(), maxRectWidth, nameDim.getHeight());

            // place swimline text
            gaService.setLocationAndSize(swimlane, 0, 0, maxRectWidth, swimlineDim.getHeight());

            // set container size as sum of image and texts dimensions
            int totalTextHeight = swimlineDim.getHeight() + nameDim.getHeight();
            gaService.setSize(box, maxRectWidth, totalTextHeight);
        }

        @Override
        public void update() {
            StartState node = (StartState) target;
            name.setValue(node.getName());
            swimlane.setValue(node.getSwimlaneLabel());

            // fit definition size for new labels
            int oldWidth = box.getWidth();
            IDimension swimlineDim = GraphitiUi.getUiLayoutService().calculateTextSize(node.getSwimlaneLabel(), getFont(swimlane));
            IDimension nameDim = GraphitiUi.getUiLayoutService().calculateTextSize(node.getName(), getFont(name));
            int maxWidth = Math.max(swimlineDim.getWidth(), nameDim.getWidth());
            box.setWidth(maxWidth);
            box.setX(box.getX() + (oldWidth - maxWidth) / 2);
            name.setWidth(maxWidth);
            swimlane.setWidth(maxWidth);

            getConstraint().setWidth(box.getWidth());
            getConstraint().setX(box.getX());
        }

        @Override
        public PictogramElement getOwner() {
            return owner;
        }

        public String getName() {
            return name.getValue();
        }

        public String getSwimlaneName() {
            return swimlane.getValue();
        }
    }

}

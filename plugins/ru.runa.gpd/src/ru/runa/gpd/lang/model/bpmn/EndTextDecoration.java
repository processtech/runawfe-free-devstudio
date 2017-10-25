package ru.runa.gpd.lang.model.bpmn;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import ru.runa.gpd.editor.graphiti.UIContainer;

public class EndTextDecoration extends TextDecorationNode {

    @Override
    public AbstractEndTextDecorated getTarget() {
        return (AbstractEndTextDecorated) target;
    }

    public class EndDefinitionUI implements UIContainer {

        private PictogramElement owner;
        private Text name;

        public EndDefinitionUI(PictogramElement owner, Text name) {
            this.owner = owner;
            this.name = name;
            pack();
        }

        @Override
        public void pack() {
        	IDimension nameDimension = GraphitiUi.getUiLayoutService().calculateTextSize(target.getName(), getFont(name));
            name.setWidth(nameDimension.getWidth());
            name.setHeight(nameDimension.getHeight());
        }

        @Override
        public void update() {
        	name.setValue(getTarget().getName());
        	IDimension nameDim = GraphitiUi.getUiLayoutService().calculateTextSize(getTarget().getName(), getFont(name));
            int oldWidth = name.getWidth();
            name.setWidth(nameDim.getWidth());
            name.setX(name.getX() + (oldWidth - nameDim.getWidth()) / 2);
            getConstraint().setWidth(name.getWidth());
            getConstraint().setX(name.getX());
        }

        @Override
        public PictogramElement getOwner() {
            return owner;
        }

    }

}

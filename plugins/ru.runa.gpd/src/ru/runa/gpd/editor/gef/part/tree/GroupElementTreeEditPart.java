package ru.runa.gpd.editor.gef.part.tree;

import java.util.List;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GroupElement;

public class GroupElementTreeEditPart extends ElementTreeEditPart {
    public GroupElementTreeEditPart(GroupElement element) {
        setModel(element);
    }

    @Override
    public GroupElement getModel() {
        return (GroupElement) super.getModel();
    }

    @Override
    protected List<? extends GraphElement> getModelChildren() {
        return getModel().getProcessDefinition().getChildren(getModel().getTypeDefinition().getModelClass());
    }

    @Override
    protected void refreshVisuals() {
        setWidgetImage(SharedImages.getImage("icons/obj/group.gif"));
        setWidgetText(getModel().getTypeDefinition().getLabel());
    }
}

package ru.runa.gpd.editor.gef.part.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GroupElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

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
        List<? extends GraphElement> list = getModel().getProcessDefinition().getChildren(getModel().getTypeDefinition().getModelClass());
        Collections.sort(list, new Comparator<GraphElement>() {

            @Override
            public int compare(GraphElement o1, GraphElement o2) {
                String name1 = o1 instanceof NamedGraphElement ? ((NamedGraphElement) o1).getName() : null;
                String name2 = o2 instanceof NamedGraphElement ? ((NamedGraphElement) o2).getName() : null;
                if (name1 == null && name2 == null) {
                    return 0;
                }
                if (name1 == null) {
                    return -1;
                }
                if (name2 == null) {
                    return 1;
                }
                return name1.compareTo(name2);
            }

        });
        return list;
    }

    @Override
    protected void refreshVisuals() {
        setWidgetImage(SharedImages.getImage("icons/obj/group.gif"));
        setWidgetText(getModel().getTypeDefinition().getLabel());
    }
}

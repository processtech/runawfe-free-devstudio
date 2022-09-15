package ru.runa.gpd.editor.outline;

import java.util.ArrayList;
import java.util.List;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GroupElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;

public class ProcessDefinitionTreeEditPart extends ElementTreeEditPart {
    @Override
    public ProcessDefinition getModel() {
        return (ProcessDefinition) super.getModel();
    }

    @Override
    protected List<GraphElement> getModelChildren() {
        List<GraphElement> result = new ArrayList<GraphElement>();
        result.addAll(getModel().getChildren(StartState.class));
        for (NodeTypeDefinition type : NodeRegistry.getDefinitions()) {
            if (StartState.class == type.getModelClass() || EndState.class == type.getModelClass()) {
                continue;
            }
            if (getModel().getLanguage() == Language.JPDL && type.getJpdlElementName() == null) {
                continue;
            }
            if (getModel().getLanguage() == Language.BPMN && type.getBpmnElementName() == null) {
                continue;
            }
            List<? extends GraphElement> elements = getModel().getChildren(type.getModelClass());
            elements.removeIf(element -> element.getClass() != type.getModelClass());
            if (elements.size() > 0) {
                result.add(new GroupElement(getModel(), type));
            }
        }
        result.addAll(getModel().getChildren(EndState.class));
        return result;
    }
}

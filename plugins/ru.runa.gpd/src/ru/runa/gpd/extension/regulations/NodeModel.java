package ru.runa.gpd.extension.regulations;

import java.util.List;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.validation.FormNodeValidation;

public class NodeModel {
    private final Node node;
    private final List<Transition> leavingTransitions;
    private final NodeRegulationsProperties properties;
    private Swimlane swimlane;

    public NodeModel(Node node) {
        this.node = node;
        this.properties = node.getRegulationsProperties();
        this.leavingTransitions = node.getLeavingTransitions();
        if (node instanceof SwimlanedNode) {
            this.swimlane = ((SwimlanedNode) node).getSwimlane();
        }
    }

    public Node getNode() {
        return node;
    }

    public NodeRegulationsProperties getProperties() {
        return properties;
    }
    
    public List<Transition> getLeavingTransitions() {
        return leavingTransitions;
    }

    public Swimlane getSwimlane() {
        return swimlane;
    }

    public boolean isInEmbeddedSubprocess() {
        return node.getProcessDefinition() instanceof SubprocessDefinition;
    }

    public boolean hasFormValidation() {
        if (node instanceof FormNode) {
            return ((FormNode) node).hasFormValidation();
        }
        return false;
    }

    public FormNodeValidation getFormNodeValidation() {
        return ((FormNode) node).getValidation(node.getProcessDefinition().getFile());
    }

    public String getLocalized(String string) {
        return Localization.getString(string);
    }
}

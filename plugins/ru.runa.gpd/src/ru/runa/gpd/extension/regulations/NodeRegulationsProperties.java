package ru.runa.gpd.extension.regulations;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;

public class NodeRegulationsProperties {
    private final GraphElement parent;
    private boolean enabledByDefault;
    private boolean enabled = true;
    private Node previousNode;
    private Node nextNode;
    private String description = "";

    public NodeRegulationsProperties(GraphElement parent) {
        this.parent = parent;
        if (NodeRegistry.hasNodeTypeDefinition(parent.getClass())) {
            setEnabledByDefault(parent.getTypeDefinition().isEnabledInRegulationsByDefault());
        }
    }

    public GraphElement getParent() {
        return parent;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
        setEnabled(enabledByDefault);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeRegulationsProperties getCopy() {
        NodeRegulationsProperties copy = new NodeRegulationsProperties(this.parent);
        copy.setEnabled(this.isEnabled());
        copy.setPreviousNode(this.getPreviousNode());
        copy.setNextNode(this.getNextNode());
        copy.setDescription(this.getDescription());
        return copy;
    }

    public boolean isValid() {
        if (enabled) {
            if (previousNode == null && !(parent instanceof StartState)) {
                return false;
            }
            if (nextNode == null && !(parent instanceof EndState) && !(parent instanceof EndTokenState)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDefault() {
        return previousNode == null && nextNode == null && Strings.isNullOrEmpty(description) && enabledByDefault == enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parent, enabled, previousNode, nextNode, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeRegulationsProperties) {
            NodeRegulationsProperties p = (NodeRegulationsProperties) obj;
            if (!Objects.equal(parent, p.parent)) {
                return false;
            }
            if (!Objects.equal(enabled, p.enabled)) {
                return false;
            }
            if (!Objects.equal(previousNode, p.previousNode)) {
                return false;
            }
            if (!Objects.equal(nextNode, p.nextNode)) {
                return false;
            }
            if (!Objects.equal(description, p.description)) {
                return false;
            }
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        if (!enabled) {
            return Localization.getString("Node.property.regulations.notSet");
        }
        String value = Localization.getString("Node.property.previousNodeInRegulations") + ": ";
        value += RegulationsUtil.getNodeLabel(getPreviousNode());
        value += ", " + Localization.getString("Node.property.nextNodeInRegulations") + ": ";
        value += RegulationsUtil.getNodeLabel(getNextNode());
        return value;
    }
}

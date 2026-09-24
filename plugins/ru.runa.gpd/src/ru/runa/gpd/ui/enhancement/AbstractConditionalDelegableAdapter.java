package ru.runa.gpd.ui.enhancement;

import java.util.List;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;

/**
 * Abstract delegable adapter for conditional event nodes that uses
 * {@link ConditionalEventModel} to manage the event configuration.
 */
public abstract class AbstractConditionalDelegableAdapter<N extends Node> implements Delegable, GraphElementAware, ProcessDefinitionAware, VariableContainer {

    protected final N node;
    protected final ConditionalEventModel model;

    public AbstractConditionalDelegableAdapter(N node) {
        this.node = node;
        this.model = ConditionalEventModel.fromXml(node.getDelegationConfiguration());
    }

    public AbstractConditionalDelegableAdapter(N node, ConditionalEventModel model) {
        this.node = node;
        this.model = model;
    }

    @Override
    public abstract String getDelegationConfiguration();

    @Override
    public abstract void setDelegationConfiguration(String configuration);

    @Override
    public String getDelegationClassName() {
        return node.getDelegationClassName();
    }

    @Override
    public void setDelegationClassName(String className) {
        node.setDelegationClassName(className);
    }

    @Override
    public abstract String getDelegationType();

    @Override
    public ProcessDefinition getProcessDefinition() {
        return node.getProcessDefinition();
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        return node.getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
        return node.getVariableNames(includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public GraphElement getGraphElement() {
        return node;
    }

    public Node getNode() {
        return node;
    }

    public ConditionalEventModel getModel() {
        return model;
    }
}

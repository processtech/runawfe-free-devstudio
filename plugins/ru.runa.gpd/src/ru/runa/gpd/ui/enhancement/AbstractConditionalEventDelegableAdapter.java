package ru.runa.gpd.ui.enhancement;

import java.util.List;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;

public abstract class AbstractConditionalEventDelegableAdapter implements Delegable, GraphElementAware, ProcessDefinitionAware, VariableContainer {

    protected final CatchEventNode node;
    protected final ConditionalEventModel model;

    public AbstractConditionalEventDelegableAdapter(CatchEventNode node) {
        this.node = node;
        this.model = ConditionalEventModel.fromXml(node.getDelegationConfiguration());
    }

    public AbstractConditionalEventDelegableAdapter(CatchEventNode node, ConditionalEventModel model) {
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
    public String getDelegationType() {
        return node.getDelegationType();
    }

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

    public CatchEventNode getNode() {
        return node;
    }

    public ConditionalEventModel getModel() {
        return model;
    }
}

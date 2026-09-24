package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;

/**
 * Adapter for conditional evet to expose expression configuration
 * as {@link Delegable}.
 *
 * <p>Works with expression part of delegation configuration stored in
 * node delegation configuration XML.
 *
 * <p>Used for: {@link CatchEventNode}
 *
 * <p><b>Warning:</b> The {@link ConditionalEventModel} is cached and
 * not synchronized if changes were made concurrently
 */
public class ConditionalEventExpressionDelegableAdapter<N extends Node & Delegable> extends AbstractConditionalDelegableAdapter<N> {

    public ConditionalEventExpressionDelegableAdapter(N node) {
        super(node);
    }

    public ConditionalEventExpressionDelegableAdapter(N node, ConditionalEventModel model) {
        super(node, model);
    }

    @Override
    public String getDelegationConfiguration() {
        return Strings.nullToEmpty(model.getExpression());
    }

    @Override
    public void setDelegationConfiguration(String expression) {
        model.setExpression(expression);
        node.setDelegationConfiguration(model.toXml());
    }

    @Override
    public String getDelegationType() {
        return node.getDelegationType();
    }
}

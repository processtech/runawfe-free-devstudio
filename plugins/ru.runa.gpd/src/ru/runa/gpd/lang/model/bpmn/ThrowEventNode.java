package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.TooltipBuilderHelper;
import ru.runa.gpd.lang.model.ISendMessageNode;

public class ThrowEventNode extends AbstractEventNode implements ISendMessageNode {

    @Override
    protected void appendExtendedTooltip(StringBuilder tooltipBuilder) {
        super.appendExtendedTooltip(tooltipBuilder);
        if (this.getTtlDuration() != null) {
            tooltipBuilder.append(TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + Localization.getString("property.message.ttl")
                    + TooltipBuilderHelper.COLON + TooltipBuilderHelper.SPACE + getTtlDuration());
        }
    }

}

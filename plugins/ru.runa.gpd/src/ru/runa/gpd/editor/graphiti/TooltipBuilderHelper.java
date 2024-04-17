package ru.runa.gpd.editor.graphiti;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.util.VariableMapping;

public class TooltipBuilderHelper {
    private static final String ELLIPSIS = "...";
    private static final int STRING_LIMIT = 100;
    private static final int LINES_LIMIT = 15;
    public static final String COLON = ":";
    public static final String NEW_LINE = "\n";
    public static final String SPACE = " ";
    public static final String INDENT = SPACE + SPACE + SPACE + SPACE;
    public static final String EMPTY_STRING = "";

    public static void addDelegableConfiguration(Delegable delegable, StringBuilder tooltipBuilder) {
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String extendedTooltip = provider.getExtendedTooltip(delegable);

        if (!extendedTooltip.isEmpty()) {
            tooltipBuilder.append(NEW_LINE + SPACE + Localization.getString("property.delegation.configuration") + COLON);
            tooltipBuilder.append(NEW_LINE + formatTooltip(extendedTooltip, NEW_LINE));
        }
    }

    public static String variableMappingsToString(List<VariableMapping> variableMappings, boolean withUsage) {
        StringBuilder stringBuilder = new StringBuilder();
        for (VariableMapping mapping : variableMappings) {
            stringBuilder.append(NEW_LINE).append(INDENT);
            if (withUsage) {
                stringBuilder.append(mapping.toString());
            } else {
                stringBuilder.append(mapping.getName() + " = " + mapping.getMappedName());
            }
        }
        return stringBuilder.toString();
    }

    private static String formatTooltip(String tooltip, String splitter) {
        StringBuilder formattedTooltipBuilder = new StringBuilder();
        List<String> lines = Arrays.asList(tooltip.split(splitter));
        int linesCounter = 0;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
            String line = iterator.next();
            if (!line.trim().isEmpty()) {
                addLine(line, formattedTooltipBuilder, !iterator.hasNext());
            }
            if (linesCounter == LINES_LIMIT) {
                addLine(ELLIPSIS, formattedTooltipBuilder, true);
                break;
            }
            linesCounter++;
        }
        return formattedTooltipBuilder.toString();
    }

    private static void addLine(String line, StringBuilder stringBuilder, boolean end) {
        if (line.length() > STRING_LIMIT) {
            stringBuilder.append(INDENT + line.substring(0, STRING_LIMIT) + ELLIPSIS + (end ? "" : NEW_LINE));
        } else {
            stringBuilder.append(INDENT + line + (end ? "" : NEW_LINE));
        }
    }

}

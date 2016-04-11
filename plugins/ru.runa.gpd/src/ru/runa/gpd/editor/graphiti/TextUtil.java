package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.ui.services.GraphitiUi;

public class TextUtil {
    public static void setTextSize(MultiText multiText, String text, int containerWidth) {
        IDimension textDimension = GraphitiUi.getUiLayoutService().calculateTextSize(text, multiText.getFont());
        int lineCount = 1;
        float testWidth = containerWidth - 5;
        if (textDimension.getWidth() > testWidth) {
            double width = textDimension.getWidth() / testWidth;
            lineCount = (int) Math.floor(width);
            if (lineCount < width) {
                lineCount++;
            }
            lineCount++;
        }
        IGaService gaService = Graphiti.getGaService();
        gaService.setSize(multiText, containerWidth, lineCount * textDimension.getHeight());
    }
}

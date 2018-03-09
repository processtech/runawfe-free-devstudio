package ru.runa.gpd.editor.graphiti;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.Style;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import ru.runa.gpd.Activator;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEvent;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;

public class StyleUtil implements PrefConstants {
    private static Map<String, StyleInitializer> initializers = new HashMap<>();

    public static Style getStateNodeOuterRectangleStyle(Diagram diagram, GraphElement graphElement) {
        String bpmnName = graphElement.getTypeDefinition().getBpmnElementName();
        return findOrCreateStyle(diagram, bpmnName + "OuterRectangle", new StateNodeOuterRectangleStyleInitializer(bpmnName));
    }

    public static Style getStateNodeBoundaryEventEllipseStyle(Diagram diagram, IBoundaryEvent boundaryEvent) {
        String bpmnName = ((GraphElement) boundaryEvent).getParent().getTypeDefinition().getBpmnElementName();
        return findOrCreateStyle(diagram, bpmnName + "BoundaryEventEllipse", new StateNodeOuterRectangleStyleInitializer(bpmnName));
    }

    public static Style getTextStyle(Diagram diagram) {
        return findOrCreateStyle(diagram, "bpmnText", new TextStyleInitializer());
    }

    public static Style getTextAnnotationPolylineStyle(Diagram diagram) {
        return findOrCreateStyle(diagram, "textAnnotationPolyline", new TextAnnotationPolylineStyleInitializer());
    }

    public static Style getTransitionPolylineStyle(Diagram diagram) {
        return findOrCreateStyle(diagram, "transitionPolyline", new TransitionPolylineStyleInitializer());
    }

    public static Style getTransitionDiamondPolylineStyle(Diagram diagram) {
        return findOrCreateStyle(diagram, "transitionDiamondPolyline", new TransitionDiamondPolylineStyleInitializer());
    }

    private static Style createStyle(Diagram diagram, String styleId, StyleInitializer styleInitializer) {
        Style style = Graphiti.getGaService().createStyle(diagram, styleId);
        styleInitializer.init(diagram, style);
        initializers.put(styleId, styleInitializer);
        return style;
    }

    private static void initColors(Diagram diagram, Style style, String bpmnName) {
        style.setForeground(getColor(diagram, bpmnName, P_BPMN_FOREGROUND_COLOR));
        style.setBackground(getColor(diagram, bpmnName, P_BPMN_BACKGROUND_COLOR));
    }

    private static Style findOrCreateStyle(Diagram diagram, String styleId, StyleInitializer styleInitializer) {
        for (Style style : diagram.getStyles()) {
            if (styleId.equals(style.getId())) {
                return style;
            }
        }
        return createStyle(diagram, styleId, styleInitializer);
    }

    private static Color getColor(Diagram diagram, String bpmnName, String propertyName) {
        String elementPropertyName = LanguageElementPreferenceNode.getBpmnPropertyName(bpmnName, propertyName);
        if (Activator.getDefault().getPreferenceStore().contains(elementPropertyName)) {
            return getColor(diagram, elementPropertyName);
        }
        return getColor(diagram, LanguageElementPreferenceNode.getBpmnDefaultPropertyName(propertyName));
    }

    private static Color getColor(Diagram diagram, String propertyName) {
        RGB colorPref = PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), propertyName);
        return Graphiti.getGaService().manageColor(diagram, new ColorConstant(colorPref.red, colorPref.green, colorPref.blue));
    }

    public static void resetStyles(Diagram diagram) {
        for (Style style : diagram.getStyles()) {
            initializers.get(style.getId()).init(diagram, style);
        }
    }

    public static abstract class StyleInitializer {

        public abstract void init(Diagram diagram, Style style);

    }

    public static class StateNodeOuterRectangleStyleInitializer extends StyleInitializer {
        private final String bpmnName;

        public StateNodeOuterRectangleStyleInitializer(String bpmnName) {
            this.bpmnName = bpmnName;
        }

        @Override
        public void init(Diagram diagram, Style style) {
            initColors(diagram, style, bpmnName);
            style.setLineWidth(2);
        }

    }

    public static class StateNodeBoundaryEventEllipseStyleInitializer extends StyleInitializer {
        private final String bpmnName;

        public StateNodeBoundaryEventEllipseStyleInitializer(String bpmnName) {
            this.bpmnName = bpmnName;
        }

        @Override
        public void init(Diagram diagram, Style style) {
            // does not work here
            // style.setFilled(Boolean.FALSE);
            style.setForeground(getColor(diagram, bpmnName, P_BPMN_FOREGROUND_COLOR));
            style.setLineWidth(2);
        }

    }

    public static class TextStyleInitializer extends StyleInitializer {

        @Override
        public void init(Diagram diagram, Style style) {
            String fontPropertyName = LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT);
            FontData fontData = PreferenceConverter.getFontData(Activator.getDefault().getPreferenceStore(), fontPropertyName);
            boolean italic = (fontData.getStyle() & SWT.ITALIC) != 0;
            boolean bold = (fontData.getStyle() & SWT.BOLD) != 0;
            Font font = Graphiti.getGaService().manageFont(diagram, fontData.getName(), fontData.getHeight(), italic, bold);
            style.setFont(font);
            Color color = getColor(diagram, LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_FONT_COLOR));
            style.setForeground(color);
            style.setBackground(color);
        }

    }

    public static class TransitionPolylineStyleInitializer extends StyleInitializer {

        @Override
        public void init(Diagram diagram, Style style) {
            Color color = getColor(diagram, "sequenceFlow", P_BPMN_TRANSITION_COLOR);
            style.setForeground(color);
            style.setBackground(color);
        }
    }

    public static class TransitionDiamondPolylineStyleInitializer extends StyleInitializer {

        @Override
        public void init(Diagram diagram, Style style) {
            style.setForeground(getColor(diagram, "sequenceFlow", P_BPMN_TRANSITION_COLOR));
            style.setBackground(getColor(diagram, LanguageElementPreferenceNode.getBpmnDefaultPropertyName(P_BPMN_BACKGROUND_COLOR)));
        }

    }

    public static class TextAnnotationPolylineStyleInitializer extends StyleInitializer {

        @Override
        public void init(Diagram diagram, Style style) {
            style.setForeground(getColor(diagram, "textAnnotation", P_BPMN_FOREGROUND_COLOR));
            style.setLineWidth(2);
        }

    }

}

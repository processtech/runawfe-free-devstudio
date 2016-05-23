package ru.runa.gpd.editor.graphiti;

import java.awt.Font;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.mm.StyleContainer;
import org.eclipse.graphiti.mm.algorithms.styles.AdaptedGradientColoredAreas;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.GradientColoredArea;
import org.eclipse.graphiti.mm.algorithms.styles.GradientColoredAreas;
import org.eclipse.graphiti.mm.algorithms.styles.LocationType;
import org.eclipse.graphiti.mm.algorithms.styles.Style;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.graphiti.util.IGradientType;
import org.eclipse.graphiti.util.IPredefinedRenderingStyle;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;

public class StyleUtil {
    public static final IColorConstant FOREGROUND = new ColorConstant(0, 0, 0);
    public static final IColorConstant BACKGROUND = new ColorConstant(255, 255, 255);
    public static final IColorConstant VERY_LIGHT_BLUE = new ColorConstant(246, 247, 255);
    public static final IColorConstant LIGHT_BLUE = new ColorConstant(3, 104, 154);
    public static final IColorConstant BPMN_CLASS_FOREGROUND = new ColorConstant(0, 0, 0);

    public static Style getStyleForEvent(Diagram diagram) {
        final String styleId = "EVENT"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            style.setForeground(getColor(PrefConstants.P_BPMN_COLOR_BASE, FOREGROUND, diagram, gaService));
            Color color = getColor(PrefConstants.P_BPMN_COLOR_BACKGROUND, new ColorConstant("FAFBFC"), diagram, gaService);
            gaService.setRenderingStyle(style, getDefaultEventColor(diagram, color));
        }
        return style;
    }

    public static Style getStyleForTask(Diagram diagram) {
        final String styleId = "TASK"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            style.setForeground(gaService.manageColor(diagram, BPMN_CLASS_FOREGROUND));
            gaService.setRenderingStyle(style, getDefaultTaskColor(diagram));
            style.setLineWidth(2);
        }
        return style;
    }

    private static AdaptedGradientColoredAreas getDefaultTaskColor(final Diagram diagram) {
        final AdaptedGradientColoredAreas agca = StylesFactory.eINSTANCE.createAdaptedGradientColoredAreas();
        agca.setDefinedStyleId("bpmnTaskStyle");
        agca.setGradientType(IGradientType.VERTICAL);
        final GradientColoredAreas defaultGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        defaultGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        final EList<GradientColoredArea> gcas = defaultGradientColoredAreas.getGradientColor();
        addGradientColoredArea(gcas, "FAFBFC", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "FFFFCC", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT, defaultGradientColoredAreas);
        final GradientColoredAreas primarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        primarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        final EList<GradientColoredArea> selectedGcas = primarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(selectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_PRIMARY_SELECTED, primarySelectedGradientColoredAreas);
        final GradientColoredAreas secondarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        secondarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        final EList<GradientColoredArea> secondarySelectedGcas = secondarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(secondarySelectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED,
                secondarySelectedGradientColoredAreas);
        return agca;
    }

    // find the style with a given id in the style-container, can return null
    private static Style findStyle(StyleContainer styleContainer, String id) {
        // find and return style
        Collection<Style> styles = styleContainer.getStyles();
        if (styles != null) {
            for (Style style : styles) {
                if (id.equals(style.getId())) {
                    return style;
                }
            }
        }
        return null;
    }

    private static AdaptedGradientColoredAreas getDefaultEventColor(Diagram diagram, Color color) {
        AdaptedGradientColoredAreas agca = StylesFactory.eINSTANCE.createAdaptedGradientColoredAreas();
        agca.setDefinedStyleId("bpmnEventStyle");
        agca.setGradientType(IGradientType.VERTICAL);
        GradientColoredAreas defaultGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        defaultGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        EList<GradientColoredArea> gcas = defaultGradientColoredAreas.getGradientColor();
        addGradientColoredArea(gcas, color, -200, LocationType.LOCATION_TYPE_RELATIVE, color, 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT, defaultGradientColoredAreas);
        GradientColoredAreas primarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        primarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        EList<GradientColoredArea> selectedGcas = primarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(selectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_END, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_START, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_PRIMARY_SELECTED, primarySelectedGradientColoredAreas);
        GradientColoredAreas secondarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        secondarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        EList<GradientColoredArea> secondarySelectedGcas = secondarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(secondarySelectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED,
                secondarySelectedGradientColoredAreas);
        return agca;
    }

    private static void addGradientColoredArea(EList<GradientColoredArea> gcas, String startColor, int locationValueStart,
            LocationType locationTypeStart, String endColor, int locationValueEnd, LocationType locationTypeEnd, Diagram diagram) {
        IGaService gaService = Graphiti.getGaService();
        addGradientColoredArea(gcas, gaService.manageColor(diagram, new ColorConstant(startColor)), locationValueStart, locationTypeStart,
                gaService.manageColor(diagram, new ColorConstant(endColor)), locationValueEnd, locationTypeEnd, diagram);
    }

    private static void addGradientColoredArea(EList<GradientColoredArea> gcas, Color startColor, int locationValueStart,
            LocationType locationTypeStart, Color endColor, int locationValueEnd, LocationType locationTypeEnd, Diagram diagram) {
        GradientColoredArea gca = StylesFactory.eINSTANCE.createGradientColoredArea();
        gcas.add(gca);
        gca.setStart(StylesFactory.eINSTANCE.createGradientColoredLocation());
        gca.getStart().setColor(startColor);
        gca.getStart().setLocationType(locationTypeStart);
        gca.getStart().setLocationValue(locationValueStart);
        gca.setEnd(StylesFactory.eINSTANCE.createGradientColoredLocation());
        gca.getEnd().setColor(endColor);
        gca.getEnd().setLocationType(locationTypeEnd);
        gca.getEnd().setLocationValue(locationValueEnd);
    }

    public static Style getStyleForTransition(Diagram diagram) {
        final String styleId = "BPMN-TRANSITION"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            Color color = getColor(PrefConstants.P_BPMN_COLOR_TRANSITION, FOREGROUND, diagram, gaService);
            style.setForeground(color);
            style.setBackground(color);
            style.setLineWidth(1);
        }
        return style;
    }

    public static Style getStyleForPolygonArrow(Diagram diagram) {
        final String styleId = "BPMN-POLYGON-ARROW"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            Color color = getColor(PrefConstants.P_BPMN_COLOR_TRANSITION, FOREGROUND, diagram, gaService);
            style.setForeground(color);
            style.setBackground(color);
            style.setLineWidth(1);
        }
        return style;
    }

    public static Style getStyleForPolygonDiamond(Diagram diagram) {
        final String styleId = "BPMN-POLYGON-DIAMOND";
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            style.setForeground(getColor(PrefConstants.P_BPMN_COLOR_TRANSITION, FOREGROUND, diagram, gaService));
            style.setBackground(getColor(PrefConstants.P_BPMN_COLOR_BACKGROUND, BACKGROUND, diagram, gaService));
            style.setLineWidth(1);
        }
        return style;
    }

    private static Color getColor(String property, IColorConstant color, Diagram diagram, IGaService gaService) {
        if (Activator.getDefault().getPreferenceStore().contains(property)) {
            RGB colorPref = PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), property);
            color = new ColorConstant(colorPref.red, colorPref.green, colorPref.blue);
        }
        return gaService.manageColor(diagram, color);
    }

    public static Style getStyleForText(Diagram diagram) {
        final String styleId = "BPMN-TEXT";
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            updateStyleForText(diagram, style);
        }
        return style;
    }

    public static void updateStyleForText(Diagram diagram, Style style) {
        if (Activator.getDefault().getPreferenceStore().contains(PrefConstants.P_BPMN_FONT)) {
            FontData fontData = PreferenceConverter.getFontData(Activator.getDefault().getPreferenceStore(), PrefConstants.P_BPMN_FONT);
            IGaService gaService = Graphiti.getGaService();
            org.eclipse.graphiti.mm.algorithms.styles.Font font = gaService.manageFont(diagram, fontData.getName(), fontData.getHeight(),
                    (fontData.getStyle() & Font.ITALIC) != 0, (fontData.getStyle() & Font.BOLD) != 0);
            style.setFont(font);

            Color color = getColor(PrefConstants.P_BPMN_COLOR_FONT, FOREGROUND, diagram, gaService);
            style.setForeground(color);
            style.setBackground(color);
        }
    }
}

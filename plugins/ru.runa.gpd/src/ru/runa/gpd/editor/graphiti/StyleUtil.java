package ru.runa.gpd.editor.graphiti;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public class StyleUtil implements PrefConstants {
    public static final IColorConstant FOREGROUND = new ColorConstant(0, 0, 0);
    public static final IColorConstant BACKGROUND = new ColorConstant(255, 255, 255);
    public static final IColorConstant VERY_LIGHT_BLUE = new ColorConstant(246, 247, 255);
    public static final IColorConstant LIGHT_BLUE = new ColorConstant(3, 104, 154);
    public static final IColorConstant BPMN_CLASS_FOREGROUND = new ColorConstant(0, 0, 0);
    private static boolean init = false;
    private static List<String> bpmnNames = new ArrayList<String>();
	static {
		bpmnNames.add("multiTask");
		bpmnNames.add("multiProcess");
		bpmnNames.add("scriptTask");
		bpmnNames.add("userTask");
		bpmnNames.add("subProcess");
		bpmnNames.add("startTextDecoration");
		bpmnNames.add("endTextDecoration");
	}

	public static Style getStyleForEvent(Diagram diagram, String bpmnName) {
        final String styleId = bpmnName+"EVENT"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        if (style == null) { // style not found - create new style
            init = true;
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
        }
        
        if (init){
        	IGaService gaService = Graphiti.getGaService();
        	Color color = null;
        	switch (bpmnName){
        	
        	case "multiTask":
        		style.setForeground(getColor(P_BPMN_MULTITASKSTATE_BASE_COLOR, FOREGROUND, diagram, gaService));
        		color =  getColor(P_BPMN_MULTITASKSTATE_BACKGROUND_COLOR, new ColorConstant("FAFBFC"), diagram, gaService);
          		break;
        	case "multiProcess":
        		color = getColors(style, P_BPMN_MULTISUBPROCESS_BASE_COLOR, P_BPMN_MULTISUBPROCESS_BACKGROUND_COLOR, gaService, diagram);
        		break;
        	case "scriptTask":
        		color = getColors(style, P_BPMN_SCRIPTTASK_BASE_COLOR, P_BPMN_SCRIPTTASK_BACKGROUND_COLOR, gaService, diagram);
            	break;
        	case "userTask":
        		color = getColors(style, P_BPMN_STATE_BASE_COLOR, P_BPMN_STATE_BACKGROUND_COLOR, gaService, diagram);
        		break;
        	case "subProcess":
        		color = getColors(style, P_BPMN_SUBPROCESS_BASE_COLOR, P_BPMN_SUBPROCESS_BACKGROUND_COLOR, gaService, diagram);
          		break;
        	default:
        		style.setForeground(getColor(PrefConstants.P_BPMN_COLOR_BASE, FOREGROUND, diagram, gaService));
            	color = getColor(PrefConstants.P_BPMN_COLOR_BACKGROUND, new ColorConstant("FAFBFC"), diagram, gaService);
            	break;
        	}
        	gaService.setRenderingStyle(style, getDefaultEventColor(diagram, color));
        }
        return style;
    }
    
    private static Color getColors(Style style, String baseColor, String backgroundColor, IGaService gaService, Diagram diagram){
    	style.setForeground(getColor(baseColor, FOREGROUND, diagram, gaService));
    	return getColor(backgroundColor, new ColorConstant("FAFBFC"), diagram, gaService);
    }
    
    public static Style getStyleForTask(Diagram diagram) {
    	final String styleId = "TASK"; //$NON-NLS-1$
        Style style = findStyle(diagram, styleId);
        
        if (style == null) { // style not found - create new style
            IGaService gaService = Graphiti.getGaService();
            style = gaService.createStyle(diagram, styleId);
            init = true;
        }
        if (init){
        	IGaService gaService = Graphiti.getGaService();
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
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED, secondarySelectedGradientColoredAreas);
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
        IGaService gaService = Graphiti.getGaService();
        Color colorEnd  = gaService.manageColor(diagram, new ColorConstant(Math.max(0, color.getRed()-1), Math.max(0, color.getGreen()-1), Math.max(0, color.getBlue()-1)));
        addGradientColoredArea(gcas, color, 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, colorEnd, 0, LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT, defaultGradientColoredAreas);
        GradientColoredAreas primarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        primarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        EList<GradientColoredArea> selectedGcas = primarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(selectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
                LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_PRIMARY_SELECTED, primarySelectedGradientColoredAreas);
        GradientColoredAreas secondarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
        secondarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
        EList<GradientColoredArea> secondarySelectedGcas = secondarySelectedGradientColoredAreas.getGradientColor();
        addGradientColoredArea(secondarySelectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C3", 0, LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
        agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED, secondarySelectedGradientColoredAreas);
        return agca;
    }

    private static void addGradientColoredArea(EList<GradientColoredArea> gcas, String colorStart, int locationValueStart, LocationType locationTypeStart, String colorEnd,
            int locationValueEnd, LocationType locationTypeEnd, Diagram diagram) {
        IGaService gaService = Graphiti.getGaService();
        addGradientColoredArea(gcas, gaService.manageColor(diagram, new ColorConstant(colorStart)), locationValueStart, locationTypeStart,
        		      gaService.manageColor(diagram, new ColorConstant(colorEnd)), locationValueEnd, locationTypeEnd, diagram);  
    }
    
    private static void addGradientColoredArea(EList<GradientColoredArea> gcas, Color startColor, int locationValueStart,
    		             LocationType locationTypeStart, Color endColor, int locationValueEnd, LocationType locationTypeEnd, Diagram diagram){
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
    		init = true;
    	}
    	if (init){
    		IGaService gaService = Graphiti.getGaService();
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
            init = true;
         }
         if (init){
           	IGaService gaService = Graphiti.getGaService();
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
            init = true;
        }
        if (init) {
        	IGaService gaService = Graphiti.getGaService();
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
    	 
    	 
    public static Style getStyleForText(Diagram diagram, String bpmnName){
    	String styleId = bpmnName+"-Text";
    	Style style = findStyle(diagram, styleId);
    	if (style == null) { // style not found - create new style
    		IGaService gaService = Graphiti.getGaService();
    		style = gaService.createStyle(diagram, styleId);
    		init = true;
    	}
    	if (init) {
    		switch (bpmnName){
    		case "scriptTask":
    			updateStyleForText(diagram, style, P_BPMN_SCRIPTTASK_FONT, P_BPMN_SCRIPTTASK_FONT_COLOR);
            	break;
        	case "userTask":
        		updateStyleForText(diagram, style, P_BPMN_STATE_FONT, P_BPMN_STATE_FONT_COLOR);
        		break;
        	case "endTokenEvent":
        		updateStyleForText(diagram, style, P_BPMN_ENDTOKEN_FONT, P_BPMN_ENDTOKEN_FONT_COLOR);
        		break;
        	case "endTextDecoration":
        		updateStyleForText(diagram, style, P_BPMN_END_FONT, P_BPMN_END_FONT_COLOR);
        		break;
        	case "startTextDecoration":
        		updateStyleForText(diagram, style, P_BPMN_STARTSTATE_FONT, P_BPMN_STARTSTATE_FONT_COLOR);
        		break;
        	case "multiTask":
        		updateStyleForText(diagram, style, P_BPMN_MULTITASKSTATE_FONT, P_BPMN_MULTITASKSTATE_FONT_COLOR);
          		break;
        	case "multiProcess":
        		updateStyleForText(diagram, style, P_BPMN_MULTISUBPROCESS_FONT, P_BPMN_MULTISUBPROCESS_FONT_COLOR);
          		break;
        	case "subProcess":
        		updateStyleForText(diagram, style, P_BPMN_SUBPROCESS_FONT, P_BPMN_SUBPROCESS_FONT_COLOR);
          		break;
        	case "transition":
        		updateStyleForText(diagram, style, P_BPMN_SUBPROCESS_FONT, P_BPMN_COLOR_TRANSITION);
          		break;
          	default :
          		updateStyleForText(diagram, style, P_BPMN_FONT, P_BPMN_COLOR_FONT);
    		}
    	}
    	return style;
    }
    	 
    private static void updateStyleForText(Diagram diagram, Style style, String font, String fontColor) {
    	if (Activator.getDefault().getPreferenceStore().contains(font)) {
    		FontData fontData = PreferenceConverter.getFontData(Activator.getDefault().getPreferenceStore(), font);
    		IGaService gaService = Graphiti.getGaService();
    	    org.eclipse.graphiti.mm.algorithms.styles.Font fontS = gaService.manageFont(diagram, fontData.getName(), fontData.getHeight(),
    	    (fontData.getStyle() & Font.ITALIC) != 0, (fontData.getStyle() & Font.BOLD) != 0);
    	    style.setFont(fontS);
    	 
    	    Color color = getColor(fontColor, FOREGROUND, diagram, gaService);
    	    style.setForeground(color);
    	    style.setBackground(color);
    	 }
    }
    	 
    public static void resetStyles(Diagram diagram) {
    	init = true;
    	for (String bpmnName : bpmnNames){
	    	getStyleForEvent(diagram, bpmnName);
	    	getStyleForTask(diagram);
	    	getStyleForTransition(diagram);
	    	getStyleForPolygonArrow(diagram);
	    	getStyleForPolygonDiamond(diagram);
	    	getStyleForText(diagram, bpmnName);
    	}
    	init = false;
    }
}

package ru.runa.gpd.ui.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class XmlHighlightTextStyling extends HighlightTextStyling {
    private static final Color COLOR_XML_ATTRIBUTE_NAME = new Color(null, 255, 55, 55);
    private static final Color COLOR_XML_ATTRIBUTE_VALUE = new Color(null, 0, 0, 255);
    private static final Color COLOR_XML_ELEMENT_TEXT = new Color(null, 0, 0, 0);
    private static final Color COLOR_XML_COMMENT = new Color(null, 155, 155, 155);
    private static final Color COLOR_XML_ELEMENT_NAME = new Color(null, 0, 155, 0);

    private static List<RegexpHighlight> highlightDefinitions = new ArrayList<RegexpHighlight>();
    static {
        addHighlight("attrName", "(?m)[a-zA-Z_][\\w\\-\\:]*?(?:\\={1})", COLOR_XML_ATTRIBUTE_NAME, SWT.NORMAL);
        addHighlight("attrValue", "(?m)\"[^\"]*\"", COLOR_XML_ATTRIBUTE_VALUE, SWT.NORMAL);
        addHighlight("tag", "(?m)((?:</?\\s*)\\b([a-zA-Z_][\\w\\-\\:\\=]*)\\b>?)|(<\\?xml)|(\\?>)|(>)", COLOR_XML_ELEMENT_NAME, SWT.NORMAL);
        addHighlight("text", "(?s)(?<=\\>)[^<>]*+(?=\\<)", COLOR_XML_ELEMENT_TEXT, SWT.NORMAL);
        addHighlight("cdata", "(?s)(?<=>)\\s*?<\\!\\[CDATA\\[.*?\\]\\]>\\s*?(?=<)", COLOR_XML_ELEMENT_TEXT, SWT.NORMAL);
        addHighlight("comment", "(?s)(<!--.*?-->)|(<!--.*?\\z)", COLOR_XML_COMMENT, SWT.ITALIC);
    }

    public static void addHighlight(String name, String regexp, Color fg, int textStyle) {
        StyleRange styleRange = new StyleRange(0, 0, fg, null, textStyle);
        RegexpHighlight highlight = new RegexpHighlight(name, regexp, false, styleRange);
        highlightDefinitions.add(highlight);
    }

    public XmlHighlightTextStyling() {
        super(highlightDefinitions);
    }

}

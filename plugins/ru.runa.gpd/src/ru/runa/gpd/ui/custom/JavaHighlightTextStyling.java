package ru.runa.gpd.ui.custom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class JavaHighlightTextStyling extends HighlightTextStyling {
    private static final Color COLOR_COMMENT = new Color(null, 155, 155, 155);
    private static final Color COLOR_JAVA_KEYWORD = new Color(null, 0, 200, 0);
    private static final Color COLOR_STRING = new Color(null, 0, 0, 200);
    private static final Color VARIABLE_COLOR = new Color(null, 155, 155, 255);

    private static List<RegexpHighlight> highlightDefinitions = new ArrayList<RegexpHighlight>();
    static {
        addHighlight(
                "keyword",
                "(?m)\\b(?:package|import|public|private|protected|static|final|native|volatile|transient|boolean|short|int|long|float|double|char|new|return|switch|case|try|catch|throws|throw|finally|if|else|for|while|do|class|interface|void|enum|null|true|false)\\b",
                COLOR_JAVA_KEYWORD, SWT.BOLD);
        addHighlight("string", "(?m)\"[^\"]*\"", COLOR_STRING, SWT.BOLD);
        addHighlight("lineComment", "(?m)//.*$", COLOR_COMMENT, SWT.ITALIC);
        addHighlight("blockComment", "(?s)/\\*.*?(?:\\*/|\\z)", COLOR_COMMENT, SWT.ITALIC);
    }

    private static void addHighlight(String name, String regexp, Color fg, int textStyle) {
        StyleRange styleRange = new StyleRange(0, 0, fg, null, textStyle);
        highlightDefinitions.add(new RegexpHighlight(name, regexp, false, styleRange));
    }

    public JavaHighlightTextStyling(List<String> variableNames) {
        super(highlightDefinitions);
        for (String variableName : variableNames) {
            StyleRange styleRange = new StyleRange(0, 0, VARIABLE_COLOR, null, SWT.NORMAL);
            addHighlightDefinition(new RegexpHighlight(variableName, variableName, true, styleRange));
        }
    }
}

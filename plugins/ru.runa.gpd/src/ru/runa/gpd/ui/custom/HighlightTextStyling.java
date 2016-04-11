package ru.runa.gpd.ui.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;

public abstract class HighlightTextStyling implements LineStyleListener {
    private final List<RegexpHighlight> highlightDefinitions;

    public HighlightTextStyling(List<RegexpHighlight> highlightDefinitions) {
        this.highlightDefinitions = highlightDefinitions;
    }

    protected void addHighlightDefinition(RegexpHighlight highlight) {
        this.highlightDefinitions.add(highlight);
    }

    @Override
    public void lineGetStyle(LineStyleEvent event) {
        int lineStart = event.lineOffset;
        List<StyleRange> lineStyleRanges = new ArrayList<StyleRange>();

        for (RegexpHighlight rh : highlightDefinitions) {
            Matcher matcher = rh.regexp.matcher(event.lineText);
            while (matcher.find()) {
                int start = matcher.start();
                int length = matcher.end() - start;
                lineStyleRanges.add(copyWithNewRegion(lineStart + start, length, rh.styleRange));
            }
        }
        event.styles = lineStyleRanges.toArray(new StyleRange[lineStyleRanges.size()]);
    }

    private StyleRange copyWithNewRegion(int start, int length, StyleRange styleRange) {
        return new StyleRange(start, length, styleRange.foreground, styleRange.background, styleRange.fontStyle);
    }

    public static class RegexpHighlight {
        final String name;
        final Pattern regexp;
        final StyleRange styleRange;

        public RegexpHighlight(String name, String regexp, boolean quote, StyleRange styleRange) {
            this.name = name;
            this.regexp = Pattern.compile(quote ? Pattern.quote(regexp) : regexp);
            this.styleRange = styleRange;
        }
    }
}

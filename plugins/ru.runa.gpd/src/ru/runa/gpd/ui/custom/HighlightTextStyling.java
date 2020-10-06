package ru.runa.gpd.ui.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;

public abstract class HighlightTextStyling implements LineStyleListener {
    private final List<RegexpHighlight> highlightDefinitions = new ArrayList<>();
    private final Map<String, StyleRange[]> cache = new HashMap<>();

    public HighlightTextStyling(List<RegexpHighlight> highlightDefinitions) {
        this.highlightDefinitions.addAll(highlightDefinitions);
    }

    protected void addHighlightDefinition(RegexpHighlight highlight) {
        this.highlightDefinitions.add(highlight);
    }

    @Override
    public void lineGetStyle(LineStyleEvent event) {
        String cacheKey = event.lineOffset + "_" + event.lineText;
        StyleRange[] ranges = cache.get(cacheKey);
        if (ranges == null) {
            Map<StyleRange, List<StyleRange>> rangesMap = new HashMap<>();
            for (RegexpHighlight rh : highlightDefinitions) {
                Matcher matcher = rh.regexp.matcher(event.lineText);
                while (matcher.find()) {
                    int start = event.lineOffset + matcher.start();
                    int length = matcher.end() - matcher.start();
                    if (!rangesMap.containsKey(rh.styleRange)) {
                        rangesMap.put(rh.styleRange, new ArrayList<>());
                    }
                    StyleRange intersectedStyleRange = null;
                    for (StyleRange styleRange : rangesMap.get(rh.styleRange)) {
                        if (start + length < styleRange.start || start > styleRange.start + styleRange.length) {
                            continue;
                        }
                        intersectedStyleRange = styleRange;
                        break;
                    }
                    if (intersectedStyleRange == null) {
                        rangesMap.get(rh.styleRange).add(copyWithNewRegion(start, length, rh.styleRange));
                    } else {
                        int newStart = Math.min(start, intersectedStyleRange.start);
                        int newEnd = Math.max(start + length, intersectedStyleRange.start + intersectedStyleRange.length);
                        int newLength = newEnd - newStart;
                        if (newStart != intersectedStyleRange.start || newLength != intersectedStyleRange.length) {
                            intersectedStyleRange.start = newStart;
                            intersectedStyleRange.length = newLength;
                        }
                    }
                }
            }
            List<StyleRange> rangesList = rangesMap.entrySet().stream().map(e -> e.getValue()).reduce(new ArrayList<>(), (result, list) -> {
                result.addAll(list);
                return result;
            });
            ranges = rangesList.toArray(new StyleRange[rangesList.size()]);
            cache.put(cacheKey, ranges);
        }
        event.styles = ranges;
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

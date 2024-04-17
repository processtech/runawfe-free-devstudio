package ru.runa.gpd.ui.custom;

import java.util.EnumSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * Featured styled text.
 * <p>
 * Features are available through {@link Feature}.
 * </p>
 * 
 * @author KuchmaMA
 * 
 */
public class FeaturedStyledText extends StyledText {

    public enum Feature {
        /**
         * Display line number.
         * <p>
         * Auto-adjusted line width.
         * </p>
         */
        LINE_NUMBER,
        /**
         * Undo-redo limited support.
         */
        UNDO_REDO,
        /**
         * Find text support.
         */
        FIND_TEXT
    }

    public FeaturedStyledText(Composite parent, int style) {
        this(parent, style, EnumSet.noneOf(Feature.class));
    }

    public FeaturedStyledText(Composite parent, int style, EnumSet<Feature> features) {
        super(parent, style);
        processFeatures(features);
    }

    private void processFeatures(EnumSet<Feature> features) {
        if (features.contains(Feature.LINE_NUMBER)) {
            addLineNumberSupport();
        }
        if (features.contains(Feature.UNDO_REDO)) {
            addUndoRedoSupport();
        }
        if (features.contains(Feature.FIND_TEXT)) {
            addFindTextSupport();
        }
    }

    private void addUndoRedoSupport() {
        new UndoRedoListener(this);
    }

    private void addLineNumberSupport() {
        final StyledText styledText = this;
        styledText.addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(LineStyleEvent event) {
                // Using ST.BULLET_NUMBER sometimes results in weird alignment.
                // event.bulletIndex =
                // styledText.getLineAtOffset(event.lineOffset);
                StyleRange styleRange = new StyleRange();
                styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
                int maxLine = styledText.getLineCount();
                int bulletLength = Integer.toString(maxLine).length();
                // Width of number character is half the height in monospaced
                // font, add 1 character width for right padding.
                int bulletWidth = (bulletLength + 1) * styledText.getLineHeight() / 2;
                styleRange.metrics = new GlyphMetrics(0, 0, bulletWidth);
                event.bullet = new Bullet(ST.BULLET_TEXT, styleRange);
                // getLineAtOffset() returns a zero-based line index.
                int bulletLine = styledText.getLineAtOffset(event.lineOffset) + 1;
                event.bullet.text = String.format("%" + bulletLength + "s", bulletLine);
            }
        });
        styledText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                // For line number redrawing.
                styledText.redraw();
            }
        });
    }

    private void addFindTextSupport() {
        new FindTextListener(this);
    }
}

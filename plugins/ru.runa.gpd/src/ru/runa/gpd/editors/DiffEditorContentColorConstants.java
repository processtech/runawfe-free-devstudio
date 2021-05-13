package ru.runa.gpd.editors;

import org.eclipse.swt.graphics.RGB;

public interface DiffEditorContentColorConstants {
    RGB DEFAULT_TEXT = new RGB(0, 0, 0);
    RGB DEFAULT_BACKGROUND = new RGB(255, 255, 255);
    // added
    RGB darkcyan = new RGB(0, 139, 139);
    // deleted
    RGB brown = new RGB(165, 42, 42);
    // comment
    RGB lightgray = new RGB(211, 211, 211);
    // unchanged
    RGB gray = new RGB(190, 190, 190);
}

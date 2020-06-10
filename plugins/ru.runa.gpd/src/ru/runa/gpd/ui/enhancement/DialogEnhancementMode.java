package ru.runa.gpd.ui.enhancement;

public class DialogEnhancementMode {

    public DialogEnhancementMode() {
        dialogOptionFlags = DEFAULT_VIEWMODE;
    }

    protected long dialogOptionFlags;

    // Dialog enhancement modes
    public static long DEFAULT_VIEWMODE = (1L << 63);
    public static long DOCX_EMBEDDED_VIEWMODE = (1L << 62);

    public boolean checkDocxEnhancementMode() {
        return is(DOCX_EMBEDDED_VIEWMODE) && not(DEFAULT_VIEWMODE);
    }

    // Common check functions
    public boolean isOrDefault(long flags) {
        return ((dialogOptionFlags & flags) != 0) || ((dialogOptionFlags & DEFAULT_VIEWMODE) != 0);
    }

    public boolean is(long flags) {
        return (dialogOptionFlags & flags) != 0;
    }

    public boolean not(long flags) {
        return !is(flags);
    }
}

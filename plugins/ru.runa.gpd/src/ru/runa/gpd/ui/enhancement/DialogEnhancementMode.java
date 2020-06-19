package ru.runa.gpd.ui.enhancement;

public class DialogEnhancementMode {

    public DialogEnhancementMode() {
        dialogOptionFlags = DEFAULT_VIEWMODE;
    }

    protected long dialogOptionFlags;
    public DialogEnhancementObserver observer;

    // Dialog enhancement modes
    public static long DEFAULT_VIEWMODE = (1L << 63);
    public static long DOCX_EMBEDDED_VIEWMODE = (1L << 62);
    public static long DOCX_SCRIPT_EMBEDDED_VIEWMODE = (1L << 61);

    // Invoke/updateObserver codes
    public static long DOCX_NO_PARAMS = 0L;
    public static long DOCX_INPUT_VARIABLE_MODE_SELECTED = (1L << 3);
    public static long DOCX_OUTPUT_VARIABLE_MODE_SELECTED = (1L << 4);
    public static long DOCX_RELOAD_FROM_TEMPLATE = (1L << 5);

    public void invoke(long flags) {
        throw new RuntimeException("Not implemented method!");
    }

    public void updateObserver(long flags) {
        if (null != observer) {
            observer.invokeEnhancementObserver(flags);
        }
    }

    public boolean checkDocxEnhancementMode() {
        return is(DOCX_EMBEDDED_VIEWMODE) && not(DOCX_SCRIPT_EMBEDDED_VIEWMODE) && not(DEFAULT_VIEWMODE);
    }

    public boolean checkScriptDocxEnhancementMode() {
        return is(DOCX_SCRIPT_EMBEDDED_VIEWMODE) && not(DOCX_EMBEDDED_VIEWMODE) && not(DEFAULT_VIEWMODE);
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

    public static boolean check(long flags, long mode) {
        return (mode & flags) != 0;
    }
}

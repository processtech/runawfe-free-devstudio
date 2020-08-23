package ru.runa.gpd.ui.enhancement;

public class DialogEnhancementMode {

    public DialogEnhancementMode() {
        dialogOptionFlags = DEFAULT_VIEWMODE;
    }

    protected long dialogOptionFlags;
    public DialogEnhancementObserver observer;

    // Dialog enhancement modes
    public static long DEFAULT_VIEWMODE = (1L << 63);
    public static long DOCX_TEMPLATE_BOT_VIEWMODE = (1L << 62);
    public static long DOCX_TEMPLATE_SCRIPT_VIEWMODE = (1L << 61);

    // Invoke/updateObserver codes
    public static long DOCX_CREATE_VARIABLE = (1L << 1);
    public static long DOCX_DELETE_VARIABLE = (1L << 2);
    public static long DOCX_INPUT_VARIABLE_MODE = (1L << 3);
    public static long DOCX_OUTPUT_VARIABLE_MODE = (1L << 4);
    public static long DOCX_SET_PROCESS_FILEPATH = (1L << 5);
    public static long DOCX_RELOAD_FROM_TEMPLATE = (1L << 6);
    public static long DOCX_MAKE_DIRTY = (1L << 7);

    public void invoke(long flags) {
        throw new RuntimeException("Not implemented method!");
    }

    public void invokeObserver(long flags) {
        if (null != observer) {
            observer.invokeEnhancementObserver(flags);
        }
    }

    // Enhancement modes check functions
    public boolean checkBotDocxTemplateEnhancementMode() {
        return is(DOCX_TEMPLATE_BOT_VIEWMODE) && not(DOCX_TEMPLATE_SCRIPT_VIEWMODE) && not(DEFAULT_VIEWMODE);
    }

    public boolean checkScriptDocxTemplateEnhancementMode() {
        return is(DOCX_TEMPLATE_SCRIPT_VIEWMODE) && not(DOCX_TEMPLATE_BOT_VIEWMODE) && not(DEFAULT_VIEWMODE);
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

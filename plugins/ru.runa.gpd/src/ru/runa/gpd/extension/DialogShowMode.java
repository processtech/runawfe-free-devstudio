package ru.runa.gpd.extension;

public class DialogShowMode {

    public DialogShowMode() {
        modeFlags = DEFAULT_VIEWMODE;
    }

    public DialogShowMode(long modeWithFlags) {
        if (0 != (modeWithFlags & DOCX_EMBEDDED_VIEWMODE)) {
            modeFlags = modeWithFlags;
        } else {
            throw new RuntimeException("Unsupported DialogViewMode!");
        }
    }

    // Dialog show modes
    public static long DEFAULT_VIEWMODE = (1L << 63);
    public static long DOCX_EMBEDDED_VIEWMODE = (1L << 62);

    // DOCX flags && functions
    public static long DOCX_SHOW_XML_VIEW = (1L << 9);
    public static long DOCX_SHOW_STRICT_LABEL = (1L << 8);
    public static long DOCX_SHOW_INPUT = (1L << 7);
    public static long DOCX_SHOW_OUTPUT = (1L << 3);

    public boolean checkDocxMode() {
        return is(DOCX_EMBEDDED_VIEWMODE) && not(DEFAULT_VIEWMODE);
    }

    // Common check functions
    public boolean isOrDefault(long flags) {
        return ((modeFlags & flags) != 0) || ((modeFlags & DEFAULT_VIEWMODE) != 0);
    }

    public boolean is(long flags) {
        return (modeFlags & flags) != 0;
    }

    public boolean not(long flags) {
        return !is(flags);
    }

    private long modeFlags;
}

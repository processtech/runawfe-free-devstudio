package ru.runa.gpd.ui.enhancement;

public class DocxDialogEnhancementMode extends DialogEnhancementMode {

    public DocxDialogEnhancementMode(long modeWithFlags) {
        dialogOptionFlags = DOCX_EMBEDDED_VIEWMODE | modeWithFlags;
    }

    public static long DOCX_SHOW_XML_VIEW = (1L << 5);
    public static long DOCX_SHOW_INPUT = (1L << 4);
    public static long DOCX_SHOW_OUTPUT = (1L << 3);

    public void reloadXmlFromModel(String xml, String embeddedFileName, Boolean enableReadDocxButton, Boolean enableDocxMode) {
        throw new RuntimeException("Not implemented method!");
    };

    public boolean checkDocxMode() {
        return null != enableDocxMode && enableDocxMode;
    }

    public Object docxModel = null;
    public String defaultFileName;
    public boolean showFileAsNewFirstTime;
    public Boolean enableDocxMode;
}

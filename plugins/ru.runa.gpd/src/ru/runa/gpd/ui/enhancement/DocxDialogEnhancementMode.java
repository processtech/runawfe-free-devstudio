package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Delegable;

public class DocxDialogEnhancementMode extends DialogEnhancementMode {

    public DocxDialogEnhancementMode(long modeWithFlags) {
        dialogOptionFlags = DOCX_TEMPLATE_BOT_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | modeWithFlags;
    }

    public DocxDialogEnhancementMode(boolean isScriptMode, long modeWithFlags) {
        if (isScriptMode) {
            dialogOptionFlags = DOCX_TEMPLATE_SCRIPT_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | DOCX_SHOW_INPUT | DOCX_SHOW_OUTPUT | modeWithFlags;
        } else {
            dialogOptionFlags = DOCX_TEMPLATE_BOT_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | modeWithFlags;
        }
    }

    // Create options
    public static long DOCX_DONT_USE_VARIABLE_TITLE = (1L << 56);
    public static long DOCX_SHOW_XML_VIEW = (1L << 55);
    public static long DOCX_SHOW_INPUT = (1L << 54);
    public static long DOCX_SHOW_OUTPUT = (1L << 53);

    public void reloadBotTaskEditorXmlFromModel(String xml, String embeddedFileName, Boolean enableReadDocxButton, Boolean enableDocxMode) {
        throw new RuntimeException("Not implemented method!");
    };

    public boolean checkDocxMode() {
        return null != enableDocxMode && enableDocxMode;
    }

    public Object docxModel = null;
    public String defaultFileName;
    // public boolean showFileAsNewFirstTime;
    public Boolean enableDocxMode;

    public static final String PLACEHOLDER_START = "${";
    public static final String PLACEHOLDER_END = "}";
    public final static String FILE_VARIABLE_FORMAT = "ru.runa.wfe.var.file.FileVariable";
    public static final String DocxHandlerID = "ru.runa.wfe.office.doc.DocxHandler";
    public static final String InputPathId = "inputPath";
    public static final String DETECT_STRING_CONST = "@_detect_^^^_cycle_@";

    public static String getInputFileParamName() {
        return Localization.getString("MSWordConfig.label.template");
    }

    public static String getOutputFileParamName() {
        return Localization.getString("MSWordConfig.label.result");
    }

    public static boolean isScriptDocxHandlerEnhancement(Delegable delegable) {
        return DialogEnhancement.isOn() && null != delegable && !Strings.isNullOrEmpty(delegable.getDelegationClassName())
                && 0 == delegable.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID);
    }
}

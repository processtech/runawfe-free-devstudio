package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
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

    static void getVariableNamesFromDocxBodyElements(List<IBodyElement> bodyElements, Map<String, Integer> variablesMap) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            if (!paragraphs.isEmpty()) {
                getVariableNamesFromDocxParagraphs(paragraphs, variablesMap);
                paragraphs.clear();
            }

            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : Lists.newArrayList(rows)) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        getVariableNamesFromDocxParagraphs(cell.getParagraphs(), variablesMap);
                    }
                }
            }

        }
        if (!paragraphs.isEmpty()) {
            getVariableNamesFromDocxParagraphs(paragraphs, variablesMap);
            paragraphs.clear();
        }

    }

    private static void getVariableNamesFromDocxParagraphs(List<XWPFParagraph> paragraphs, Map<String, Integer> variablesMap) {
        for (XWPFParagraph paragraph : Lists.newArrayList(paragraphs)) {
            String paragraphText = paragraph.getText();
            while (true) {
                if (!paragraphText.contains(DocxDialogEnhancementMode.PLACEHOLDER_START)) {
                    break;
                }
                paragraphText = paragraphText.substring(
                        paragraphText.indexOf(DocxDialogEnhancementMode.PLACEHOLDER_START) + DocxDialogEnhancementMode.PLACEHOLDER_START.length());
                if (!paragraphText.contains(DocxDialogEnhancementMode.PLACEHOLDER_END)) {
                    break;
                }
                String var = paragraphText.substring(0, paragraphText.indexOf(DocxDialogEnhancementMode.PLACEHOLDER_END));

                if (!variablesMap.containsKey(var)) {
                    variablesMap.put(var, 1);
                }

                paragraphText = paragraphText.substring(
                        paragraphText.indexOf(DocxDialogEnhancementMode.PLACEHOLDER_END) + DocxDialogEnhancementMode.PLACEHOLDER_END.length());
            }
        }
    }
}

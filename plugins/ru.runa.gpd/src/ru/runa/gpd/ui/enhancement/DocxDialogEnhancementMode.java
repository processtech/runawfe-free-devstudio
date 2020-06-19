package ru.runa.gpd.ui.enhancement;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import ru.runa.gpd.Localization;

public class DocxDialogEnhancementMode extends DialogEnhancementMode {

    public DocxDialogEnhancementMode(long modeWithFlags) {
        dialogOptionFlags = DOCX_EMBEDDED_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | modeWithFlags;
    }

    public DocxDialogEnhancementMode(boolean isScriptMode, long modeWithFlags) {
        if (isScriptMode) {
            dialogOptionFlags = DOCX_SCRIPT_EMBEDDED_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | DOCX_SHOW_INPUT | DOCX_SHOW_OUTPUT | modeWithFlags;
        } else {
            dialogOptionFlags = DOCX_EMBEDDED_VIEWMODE | DOCX_DONT_USE_VARIABLE_TITLE | modeWithFlags;
        }
    }

    public static long DOCX_DONT_USE_VARIABLE_TITLE = (1L << 6);
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

    public static final String PLACEHOLDER_START = "${";
    public static final String PLACEHOLDER_END = "}";
    public final static String FILE_VARIABLE_FORMAT = "ru.runa.wfe.var.file.FileVariable";
    public static final String DocxHandlerID = "ru.runa.wfe.office.doc.DocxHandler";

    public static String getInputFileParamName() {
        return Localization.getString("MSWordConfig.label.template");
    }

    public static String getOutputFileParamName() {
        return Localization.getString("MSWordConfig.label.result");
    }

    public static Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream) {
        Map<String, Integer> variablesMap = new HashMap<String, Integer>();
        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (XWPFHeader header : document.getHeaderList()) {
                getVariableNamesFromDocxBodyElements(header.getBodyElements(), variablesMap);
            }
            getVariableNamesFromDocxBodyElements(document.getBodyElements(), variablesMap);
            for (XWPFFooter footer : document.getFooterList()) {
                getVariableNamesFromDocxBodyElements(footer.getBodyElements(), variablesMap);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return variablesMap;
    }

    private static void getVariableNamesFromDocxBodyElements(List<IBodyElement> bodyElements, Map<String, Integer> variablesMap) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
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

package ru.runa.gpd.util.docx;

import com.google.common.collect.Lists;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import ru.runa.gpd.PluginLogger;

public class DocxVariableParser {

    public static Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream, boolean scriptParseMode) {
        Map<String, Integer> variablesMap = new TreeMap<String, Integer>();
        VariableConsumer variableProvider = new VariableConsumer(variablesMap);
        DocxConfig config = new DocxConfig();

        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (XWPFHeader header : document.getHeaderList()) {
                getVariableNamesFromDocxBodyElements(config, variableProvider, header.getBodyElements(), scriptParseMode);
            }
            getVariableNamesFromDocxBodyElements(config, variableProvider, document.getBodyElements(), scriptParseMode);
            for (XWPFFooter footer : document.getFooterList()) {
                getVariableNamesFromDocxBodyElements(config, variableProvider, footer.getBodyElements(), scriptParseMode);
            }
        } catch (Throwable exception) {
            PluginLogger.logErrorWithoutDialog(exception.getMessage(), exception);
            return null;
        }
        return variablesMap;
    }

    private static void getVariableNamesFromDocxBodyElements(DocxConfig config, VariableConsumer variableProvider, List<IBodyElement> bodyElements,
            boolean scriptParseMode) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            if (!paragraphs.isEmpty()) {
                DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs, scriptParseMode);
                paragraphs.clear();
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : Lists.newArrayList(rows)) {
                    List<XWPFTableCell> cells = row.getTableCells();

                    for (XWPFTableCell cell : cells) {
                        DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs(), scriptParseMode);
                    }

                }
            }
        }
        if (!paragraphs.isEmpty()) {
            DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs, scriptParseMode);
            paragraphs.clear();
        }
    }

}

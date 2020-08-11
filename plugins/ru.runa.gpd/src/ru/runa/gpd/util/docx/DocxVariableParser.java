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

    public static Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream) {
        Map<String, Integer> variablesMap = new TreeMap<String, Integer>();
        VariableProvider variableProvider = new VariableProvider(variablesMap);
        DocxConfig config = new DocxConfig();

        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (XWPFHeader header : document.getHeaderList()) {
                getVariableNamesFromDocxBodyElements(config, variableProvider, header.getBodyElements());
            }
            getVariableNamesFromDocxBodyElements(config, variableProvider, document.getBodyElements());
            for (XWPFFooter footer : document.getFooterList()) {
                getVariableNamesFromDocxBodyElements(config, variableProvider, footer.getBodyElements());
            }
        } catch (Throwable exception) {
            PluginLogger.logErrorWithoutDialog(exception.getMessage(), exception);
            return null;
        }
        return variablesMap;
    }

    private static void getVariableNamesFromDocxBodyElements(DocxConfig config, VariableProvider variableProvider, List<IBodyElement> bodyElements) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            if (!paragraphs.isEmpty()) {
                DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
                paragraphs.clear();
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : Lists.newArrayList(rows)) {
                    List<XWPFTableCell> cells = row.getTableCells();

                    for (XWPFTableCell cell : cells) {
                        DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs());
                    }

                    // // try to expand cells by column
                    // TableExpansionOperation tableExpansionOperation = new TableExpansionOperation(row);
                    // boolean wasCycle = false;
                    // for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
                    // final XWPFTableCell cell = cells.get(columnIndex);
                    // ColumnExpansionOperation operation = DocxUtils.parseIterationOperation(config, variableProvider, cell.getText(),
                    // new ColumnExpansionOperation());
                    // if (operation != null && operation.isValid()) {
                    // tableExpansionOperation.addOperation(columnIndex, operation);
                    // } else {
                    // operation = new ColumnSetValueOperation();
                    // operation.setContainerValue(cell.getText());
                    // tableExpansionOperation.addOperation(columnIndex, operation);
                    // }
                    // String text0 = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, 0);
                    // // modify original algorithm
                    // if (text0.compareTo(DocxDialogEnhancementMode.DETECT_STRING_CONST) == 0) {
                    // // if (!java.util.Objects.equals(text0, cell.getText())) {
                    // // DocxUtils.setCellText(cell, text0);
                    // wasCycle = true;
                    // }
                    // }
                    // if (tableExpansionOperation.getRows() == 0) {
                    // if (!wasCycle) {
                    // for (XWPFTableCell cell : cells) {
                    // DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs());
                    // }
                    // }
                    // } else {
                    // int templateRowIndex = table.getRows().indexOf(tableExpansionOperation.getTemplateRow());
                    // for (int rowIndex = 1; rowIndex < tableExpansionOperation.getRows(); rowIndex++) {
                    // XWPFTableRow dynamicRow = table.insertNewTableRow(templateRowIndex + rowIndex);
                    // for (int columnIndex = 0; columnIndex < tableExpansionOperation.getTemplateRow().getTableCells().size(); columnIndex++) {
                    // dynamicRow.createCell();
                    // }
                    // for (int columnIndex = 0; columnIndex < dynamicRow.getTableCells().size(); columnIndex++) {
                    // String text = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, rowIndex);
                    // DocxUtils.setCellText(dynamicRow.getCell(columnIndex), text, tableExpansionOperation.getTemplateCell(columnIndex));
                    // if (OfficeProperties.getDocxPlaceholderVMerge().equals(text)) {
                    // CTTcPr tcPr = dynamicRow.getCell(columnIndex).getCTTc().getTcPr();
                    // tcPr.addNewVMerge().setVal(STMerge.CONTINUE);
                    //
                    // int restartVMergeRowIndex = table.getRows().indexOf(dynamicRow) - 1;
                    // if (restartVMergeRowIndex >= 0) {
                    // XWPFTableRow restartRow = table.getRow(restartVMergeRowIndex);
                    // CTTcPr previousTcPr = restartRow.getCell(columnIndex).getCTTc().getTcPr();
                    // if (previousTcPr.getVMerge() == null) {
                    // previousTcPr.addNewVMerge().setVal(STMerge.RESTART);
                    // }
                    // }
                    // }
                    // }
                    // }
                    // }
                }
            }
        }
        if (!paragraphs.isEmpty()) {
            DocxUtils.replaceInParagraphs(config, variableProvider, paragraphs);
            paragraphs.clear();
        }
    }

}

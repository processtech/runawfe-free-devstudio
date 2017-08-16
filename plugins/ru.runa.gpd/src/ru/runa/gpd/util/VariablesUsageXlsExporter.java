package ru.runa.gpd.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.collect.Sets;

public class VariablesUsageXlsExporter {
    
    private static final String PATTERN_DISPLAY_VARIABLE = "$'{'DisplayVariable(\"{0}\"";
    private static final String PATTERN_INPUT_VARIABLE = "$'{'InputVariable(\"{0}\"";

    private static List<Variable> variables;
    private static Set<Variable> usedVariables = Sets.newHashSet();
    
    public static void go(ProcessDefinition definition, String filePath) throws Exception {
        try (OutputStream os = new FileOutputStream(filePath)) {
            go(definition, os);
        }
    }
    
    public static void go(ProcessDefinition definition, OutputStream os) throws Exception {
        fillBook(definition).write(os);
        os.flush();
    }
    
    private static HSSFWorkbook fillBook(ProcessDefinition pd) throws Exception {
        variables = pd.getVariables(true, false);
        Collections.sort(variables);
        HSSFWorkbook book = new HSSFWorkbook();
        fillSheet(book, pd);
        List<Subprocess> embedded = new ArrayList<>();
        List<Subprocess> external = new ArrayList<>();
        for (Subprocess sp : pd.getChildren(Subprocess.class)) {
            if (sp.isEmbedded()) {
                embedded.add(sp);
            } else {
                external.add(sp);
            }
        }
        for (Subprocess sp : embedded) {
            fillSheet(book, sp);
        }
        if (external.size() > 0) {
            fillSheet(book, external);
        }
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            HSSFSheet sheet = book.getSheetAt(i);
            HSSFCellStyle style = book.createCellStyle();
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            int rowNum = 1;
            for (Variable variable : variables) {
                HSSFRow row = sheet.getRow(rowNum++);
                if (!usedVariables.contains(variable)) {
                    row.getCell(0).setCellStyle(style);
                }
            }
        }
        return book;
    }
    
    private static void fillSheet(HSSFWorkbook book, NamedGraphElement ge) throws Exception {
        HSSFSheet sheet = book.createSheet(ge.getName());
        Set<String> usedForms = Sets.newHashSet();
        List<String> forms = fillHeader(sheet, ge);
        int rowNum = 1;
        for (Variable variable : variables) {
            HSSFRow row = sheet.createRow(rowNum++);
            HSSFCell cell = row.createCell(0);
            cell.setCellValue(variable.getName());
            HSSFCellStyle style = book.createCellStyle();
            style.setAlignment(CellStyle.ALIGN_CENTER);
            for (int i = 0; i < forms.size(); i++) {
                String form = forms.get(i);
                if (form.contains(MessageFormat.format(PATTERN_INPUT_VARIABLE, variable.getName()))) {
                    cell = row.createCell(i + 1);
                    cell.setCellValue("W");
                    cell.setCellStyle(style);
                    usedVariables.add(variable);
                    usedForms.add(form);
                } else if (form.contains(MessageFormat.format(PATTERN_DISPLAY_VARIABLE, variable.getName()))) {
                    cell = row.createCell(i + 1);
                    cell.setCellValue("R");
                    cell.setCellStyle(style);
                    usedVariables.add(variable);
                    usedForms.add(form);
                }
                sheet.setColumnWidth(i + 1, 5 * 256);
            }
        }
        for (int i = 0; i < forms.size(); i++) {
            String form = forms.get(i);
            if (!usedForms.contains(form)) {
                HSSFRow header = sheet.getRow(0);
                HSSFCellStyle style = header.getCell(i + 1).getCellStyle();
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            }
        }
        sheet.autoSizeColumn(0);
    }
    
    private static void fillSheet(HSSFWorkbook book, List<Subprocess> subprocesses) throws Exception {
        HSSFSheet sheet = book.createSheet(Localization.getString("DesignerVariableEditorPage.report.variablesUsage.subprocesses"));
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(Localization.getString("DesignerVariableEditorPage.report.variablesUsage.header2"));
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cell.setCellStyle(style);
        int colNum = 1;
        for (Subprocess subprocess : subprocesses) {
            cell = row.createCell(colNum++);
            cell.setCellValue(subprocess.getLabel());
            style = sheet.getWorkbook().createCellStyle();
            style.setRotation((short) 90);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            cell.setCellStyle(style);
        }
        int rowNum = 1;
        for (Variable variable : variables) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(variable.getName());
            style = book.createCellStyle();
            style.setAlignment(CellStyle.ALIGN_CENTER);
        }
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            for (int j = 0; j < subprocesses.size(); j++) {
                Subprocess subprocess = subprocesses.get(j);
                List<VariableMapping> varMap = subprocess.getVariableMappings();
                for (VariableMapping vm : varMap) {
                    if (vm.getName().equals(variable.getName())) {
                        sheet.getRow(i + 1).createCell(j + 1).setCellValue(vm.getMappedName() + ": " + vm.getUsage());
                        sheet.autoSizeColumn(j + 1);
                        usedVariables.add(variable);
                    }
                }
            }
        }
        sheet.autoSizeColumn(0);
    }
    
    private static List<String> fillHeader(HSSFSheet sheet, NamedGraphElement ge) throws Exception {
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(Localization.getString("DesignerVariableEditorPage.report.variablesUsage.header1"));
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setWrapText(true);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cell.setCellStyle(style);
        List<FormNode> formNodes = ge.getChildren(FormNode.class);
        if (ge instanceof Subprocess) {
            formNodes = ((Subprocess) ge).getEmbeddedSubprocess().getChildren(FormNode.class);
        }
        Map<String, FormNode> nodes = new TreeMap<>();
        for (FormNode fn : formNodes) {
            if (fn.hasForm()) {
                Rectangle rect = fn.getConstraint();
                if (rect != null) {
                    nodes.put(String.format("%1$09d:%2$09d", rect.y + rect.height / 2, rect.x + rect.width / 2), fn);
                }
            }
        }
        List<String> forms = new ArrayList<>();
        int colNum = 1;
        for (FormNode form : nodes.values()) {
            cell = row.createCell(colNum++);
            cell.setCellValue(form.getLabel());
            style = sheet.getWorkbook().createCellStyle();
            style.setRotation((short) 90);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            cell.setCellStyle(style);
            forms.add(IOUtils.readStream(ge.getProcessDefinition().getFile().getParent().getFolder(null).getFile(form.getFormFileName()).getContents()));
        }
        return forms;
    }

}

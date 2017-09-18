package ru.runa.gpd.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class VariablesUsageXlsExporter {
    
    private static List<Variable> variables;
    private static Set<Variable> usedVariables = Sets.newHashSet();
    private static Map<String, String[]> componentParamAccess = Maps.newHashMap();

    static {
        try {
            for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.form.ftl.components").getExtensions()) {
                for (IConfigurationElement componentElement : extension.getConfigurationElements()) {
                    try {
                        List<String> access = Lists.newArrayList();
                        for (IConfigurationElement paramElement : componentElement.getChildren()) {
                            String varAccess = paramElement.getAttribute("variableAccess");
                            access.add("NONE".equals(varAccess) ? "" : varAccess.substring(0, 1));
                        }
                        componentParamAccess.put(componentElement.getAttribute("id"), access.toArray(new String[] {}));
                    } catch (Throwable th) {
                        PluginLogger.logError("Unable to load FTL component " + componentElement, th);
                    }
                }
            }
        } catch (Throwable th) {
            PluginLogger.logError("Unable to load FTL components", th);
        }
    }
    
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
        List<Subprocess> external = new ArrayList<>();
        for (Subprocess sp : pd.getChildren(Subprocess.class)) {
             if (!sp.isEmbedded()) {
                external.add(sp);
            }
        }
        if (external.size() > 0) {
            fillSheet(book, external);
        }
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            HSSFSheet sheet = book.getSheetAt(i);
            HSSFCellStyle style = book.createCellStyle();
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            HSSFCellStyle style2 = sheet.getWorkbook().createCellStyle();
            style2.setAlignment(CellStyle.ALIGN_CENTER);
            int rowNum = 1;
            for (Variable variable : variables) {
                HSSFRow row = sheet.getRow(rowNum++);
                HSSFCell cell = row.createCell(1);
                cell.setCellStyle(style2);
                if (!usedVariables.contains(variable)) {
                    row.getCell(0).setCellStyle(style);
                    cell.setCellValue("N");
                } else {
                    cell.setCellValue("Y");
                }
            }
        }
        return book;
    }
    
    private static String asSortValue(String phrase) {
        // #9-9A-A.9-9A-A.9-9A-A. AAAAA...
        phrase = phrase.trim();
        List<String> digitParts = Lists.newArrayList();
        List<String> letterParts = Lists.newArrayList();
        if (phrase.startsWith("#")) {
            try {
                String digitPart = "";
                String letterPart = "";
                int i = 0;
                for (i = 1; i < phrase.length(); i++) {
                    char ch = phrase.charAt(i);
                    if (Character.isSpaceChar(ch) || ch == '.') {
                        if (!digitPart.isEmpty() || !letterPart.isEmpty()) {
                            digitParts.add(digitPart);
                            digitPart = "";
                            letterParts.add(letterPart);
                            letterPart = "";
                        }
                        if (Character.isSpaceChar(ch)) {
                            break;
                        }
                    } else if (Character.isDigit(ch)) {
                        digitPart += ch;
                    } else {
                        letterPart += ch;
                    }
                }
                if (!digitPart.isEmpty() || !letterPart.isEmpty()) {
                    digitParts.add(digitPart);
                    digitPart = "";
                    letterParts.add(letterPart);
                    letterPart = "";
                }
                String value = "#";
                for (int j = 0; j < digitParts.size(); j++) {
                    value += (Strings.padStart(digitParts.get(j), 10, '0') + Strings.padEnd(letterParts.get(j), 10, ' ') + '.');
                }
                value += phrase.substring(i);
                return value;
            } catch (Exception e) {
                return phrase;
            }
        } else {
            return phrase;
        }
    }

    private static Collection<FormNode> gatherForms(NamedGraphElement ge, Map<String, FormNode> formMap) throws Exception {
        if (formMap == null) {
            formMap = new TreeMap<>();
        }
        List<FormNode> formNodes;
        if (ge instanceof Subprocess) {
            formNodes = ((Subprocess) ge).getEmbeddedSubprocess().getChildren(FormNode.class);
        } else {
            formNodes = ge.getChildren(FormNode.class);
        }
        for (FormNode fn : formNodes) {
            if (fn.hasForm()) {
                if (fn instanceof StartState) {
                    formMap.put(String.valueOf((char) 0), fn);
                } else {
                    formMap.put(asSortValue(fn.getLabel()), fn);
                }
            }
        }
        for (Subprocess sp : ge.getChildren(Subprocess.class)) {
            if (sp.isEmbedded()) {
                gatherForms(sp, formMap);
            }
        }
        return formMap.values();
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
        Collection<FormNode> formNodes = gatherForms(ge, null);
        cell = row.createCell(1);
        cell.setCellValue(Localization.getString("DesignerVariableEditorPage.report.variablesUsage.variable_used"));
        style = sheet.getWorkbook().createCellStyle();
        style.setRotation((short) 90);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        cell.setCellStyle(style);
        List<String> forms = new ArrayList<>();
        int colNum = 2;
        for (FormNode form : formNodes) {
            cell = row.createCell(colNum++);
            cell.setCellValue((form instanceof StartState && Strings.isNullOrEmpty(form.getName()) ?
                    Localization.getString("DesignerVariableEditorPage.report.variablesUsage.start") : "") + form.getLabel());
            cell.setCellStyle(style);
            forms.add(IOUtils.readStream(ge.getProcessDefinition().getFile().getParent().getFolder(null).getFile(form.getFormFileName()).getContents()));
        }
        return forms;
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
            String search = "\"" + variable.getName() + "\"";
            for (int i = 0; i < forms.size(); i++) {
                String form = forms.get(i);
                String varAsccess = "";
                int index1 = 0;
                int index2 = form.indexOf(search, index1);
                while (index2 > index1) {
                    int paramNumber = 0;
                    boolean compNameProcessing = false;
                    String compName = "";
                    for (int index = index2; index > index1; index--) {
                        char ch = form.charAt(index);
                        if (ch == '(') {
                            compNameProcessing = true;
                        } else if (ch == '{') {
                            break;
                        } else if (ch == ',') {
                            paramNumber++;
                        } else if (compNameProcessing) {
                            compName = ch + compName;
                        }
                    }
                    String[] paramAccess = componentParamAccess.get(compName);
                    if (paramAccess != null) {
                        String access = paramAccess[paramNumber];
                        varAsccess += access;
                    }
                    usedVariables.add(variable);
                    usedForms.add(form);
                    index1 = index2 + 1;
                    index2 = form.indexOf(search, index1);
                }
                cell = row.createCell(i + 2);
                cell.setCellValue(varAsccess);
                cell.setCellStyle(style);
            }
        }
        for (int i = 0; i < forms.size(); i++) {
            String form = forms.get(i);
            if (!usedForms.contains(form)) {
                HSSFRow header = sheet.getRow(0);
                HSSFCellStyle style = header.getCell(i + 2).getCellStyle();
                style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            }
            sheet.setColumnWidth(i + 2, 5 * 256);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
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
        cell = row.createCell(1);
        cell.setCellValue(Localization.getString("DesignerVariableEditorPage.report.variablesUsage.variable_used"));
        style = sheet.getWorkbook().createCellStyle();
        style.setRotation((short) 90);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        cell.setCellStyle(style);
        int colNum = 2;
        for (Subprocess subprocess : subprocesses) {
            cell = row.createCell(colNum++);
            cell.setCellValue(subprocess.getLabel());
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
                        sheet.getRow(i + 1).createCell(j + 2).setCellValue(vm.getMappedName() + ": " + vm.getUsage());
                        sheet.autoSizeColumn(j + 2);
                        usedVariables.add(variable);
                    }
                }
            }
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
}

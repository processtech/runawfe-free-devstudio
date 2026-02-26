package ru.runa.gpd.office.excel;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;

public class ConstraintsModel {
    public static final int CELL = 1;
    public static final int ROW = 2;
    public static final int COLUMN = 3;

    public static final String CELL_CLASS = "ru.runa.wfe.office.excel.CellConstraints";
    public static final String ROW_CLASS = "ru.runa.wfe.office.excel.RowConstraints";
    public static final String COLUMN_CLASS = "ru.runa.wfe.office.excel.ColumnConstraints";

    public String sheetName;
    public int sheetIndex = 1;
    public String variableName;
    public final int type;
    public int row = 1;
    public int column = 1;
    public final List<ColumnMapping> columns = new ArrayList<>();

    public static class ColumnMapping {
        public String attributeName;
        public int column;

        public ColumnMapping() {
        }

        public ColumnMapping(String attributeName, int column) {
            this.attributeName = attributeName;
            this.column = column;
        }
    }

    public ConstraintsModel(int type) {
        this.type = type;
    }

    public static ConstraintsModel deserialize(Element bindingElement) {
        int type = defineTypeOfClazz(bindingElement);
        ConstraintsModel model = new ConstraintsModel(type);
        model.variableName = bindingElement.attributeValue("variable");
        Element configElement = bindingElement.element("config");
        if (configElement != null) {
            String sheetValue = getSheetValue(configElement);
            applySheetToModel(sheetValue, model);
            applyCoordinatesToModel(type, configElement, model);
        }
        return applyMappingsToModel(bindingElement, model);
    }

    private static int defineTypeOfClazz(Element bindingElement) {
        String className = bindingElement.attributeValue("class");
        if (ROW_CLASS.equals(className)) {
            return ROW;
        }
        if (COLUMN_CLASS.equals(className)) {
            return COLUMN;
        }
        return CELL;
    }

    private static String getSheetValue(Element configElement) {
        String sheetValue = configElement.attributeValue("sheet");
        if (sheetValue == null) {
            sheetValue = configElement.attributeValue("sheetName");
            if (sheetValue == null) {
                sheetValue = configElement.attributeValue("sheetIndex");
            }
        }
        return sheetValue;
    }

    private static void applySheetToModel(String sheetValue, ConstraintsModel model) {
        if (sheetValue != null) {
            try {
                model.sheetIndex = Integer.parseInt(sheetValue);
            } catch (NumberFormatException e) {
                model.sheetName = sheetValue;
            }
        }
    }

    private static void applyCoordinatesToModel(int type, Element configElement, ConstraintsModel model) {
        if (type == CELL) {
            model.row = getIntAttr(configElement, "row", 1);
            model.column = getIntAttr(configElement, "column", 1);
        } else if (type == ROW) {
            model.row = getIntAttr(configElement, "row", 1);
            model.column = getIntAttr(configElement, "columnStart", 1);
        } else if (type == COLUMN) {
            model.column = getIntAttr(configElement, "column", 1);
            model.row = getIntAttr(configElement, "rowStart", 1);
        }
    }

    private static int getIntAttr(Element element, String attrName, int defaultValue) {
        String value = element.attributeValue(attrName);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    private static ConstraintsModel applyMappingsToModel(Element bindingElement, ConstraintsModel model) {
        List<Element> mappingElements = bindingElement.elements("mapping");
        for (Element mappingElement : mappingElements) {
            String field = mappingElement.attributeValue("field");
            String col = mappingElement.attributeValue("column");
            if (field != null && col != null) {
                model.columns.add(new ColumnMapping(field, Integer.parseInt(col)));
            }
        }
        return model;
    }

    public void serialize(Document document, Element root) {
        Element bindingElement = root.addElement("binding");
        bindingElement.addAttribute("class", getClassNameByType());
        if (!Strings.isNullOrEmpty(variableName)) {
            bindingElement.addAttribute("variable", variableName);
        }
        serializeConfig(bindingElement);
        serializeMappings(bindingElement);
    }

    private String getClassNameByType() {
        if (type == ROW) {
            return ROW_CLASS;
        }
        if (type == COLUMN) {
            return COLUMN_CLASS;
        }
        return CELL_CLASS;
    }

    private void serializeConfig(Element bindingElement) {
        Element configElement = bindingElement.addElement("config");
        String sheetValue = !Strings.isNullOrEmpty(sheetName) ? sheetName : String.valueOf(sheetIndex);
        configElement.addAttribute("sheet", sheetValue);
        if (type == CELL) {
            configElement.addAttribute("row", String.valueOf(row));
            configElement.addAttribute("column", String.valueOf(column));
        } else if (type == ROW) {
            configElement.addAttribute("row", String.valueOf(row));
            configElement.addAttribute("columnStart", String.valueOf(column));
        } else if (type == COLUMN) {
            configElement.addAttribute("column", String.valueOf(column));
            configElement.addAttribute("rowStart", String.valueOf(row));
        }
    }

    private void serializeMappings(Element bindingElement) {
        if (type == COLUMN || type == ROW) {
            for (ColumnMapping mapping : columns) {
                Element mapElement = bindingElement.addElement("mapping");
                mapElement.addAttribute("field", mapping.attributeName);
                mapElement.addAttribute("column", String.valueOf(mapping.column));
            }
        }
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
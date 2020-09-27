package ru.runa.gpd.office.store;

import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.util.BackCompatibilityUtils;

public class StorageConstraintsModel {
    public static final int CELL = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final int ATTR = 3;
    public static final String CELL_CLASS = "ru.runa.wfe.office.excel.CellConstraints";
    public static final String ATTR_CLASS = "ru.runa.wfe.office.excel.AttributeConstraints";
    public static final String ROW_CLASS = "ru.runa.wfe.office.excel.RowConstraints";
    public static final String COLUMN_CLASS = "ru.runa.wfe.office.excel.ColumnConstraints";
    public String sheetName = "";
    public int sheetIndex = 1;
    public String variableName = "";
    public final int type;
    public int row = 1;
    public int column = 1;
    private QueryType queryType;
    private String queryString;

    public StorageConstraintsModel(int type, QueryType queryType) {
        this.type = type;
        this.queryType = queryType;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variable) {
        this.variableName = variable;
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

    public int getType() {
        return type;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public static StorageConstraintsModel deserialize(Element element) {
        StorageConstraintsModel model;
        String className = element.attributeValue("class");
        className = BackCompatibilityUtils.getClassName(className);

        Element conditionEl = element.element("condition");
        String query = "";
        if (conditionEl != null) {
            query = conditionEl.attributeValue("query");
        }
        Element conditions = element.element("conditions");
        QueryType queryType = null;
        if (conditions != null) {
            queryType = QueryType.valueOf(conditions.attributeValue("type"));
        }
        if (ATTR_CLASS.equals(className)) {
            model = new StorageConstraintsModel(ATTR, queryType);
        } else if (CELL_CLASS.equals(className)) {
            model = new StorageConstraintsModel(CELL, queryType);
        } else if (ROW_CLASS.equals(className)) {
            model = new StorageConstraintsModel(ROW, queryType);
        } else if (COLUMN_CLASS.equals(className)) {
            model = new StorageConstraintsModel(COLUMN, queryType);
        } else {
            throw new RuntimeException("Invaid class '" + className + "'");
        }
        model.setQueryString(query);
        model.variableName = element.attributeValue("variable");
        Element conf = element.element("config");
        model.sheetName = conf.attributeValue("sheetName");
        if (model.sheetName == null || model.sheetName.length() == 0) {
            model.sheetIndex = Integer.parseInt(conf.attributeValue("sheet"));
        }
        if (model.type == CELL || model.type == ROW) {
            model.row = Integer.parseInt(conf.attributeValue("row"));
        } else if (model.type == COLUMN) {
            model.row = Integer.parseInt(conf.attributeValue("rowStart"));
        }
        if (model.type == CELL || model.type == COLUMN || model.type == ATTR) {
            model.column = Integer.parseInt(conf.attributeValue("column"));
        } else {
            model.column = Integer.parseInt(conf.attributeValue("columnStart"));
        }
        return model;
    }

    public void serialize(Document document, Element root) {
        Element binding = root.addElement("binding");
        Element config = binding.addElement("config");
        if (variableName != null && !variableName.trim().isEmpty()) {
            binding.addAttribute("variable", variableName);
        }
        switch (type) {
        case ATTR:
            binding.addAttribute("class", ATTR_CLASS);
            break;
        case CELL:
            binding.addAttribute("class", CELL_CLASS);
            break;
        case ROW:
            binding.addAttribute("class", ROW_CLASS);
            break;
        case COLUMN:
            binding.addAttribute("class", COLUMN_CLASS);
            break;
        }
        if (sheetName != null && sheetName.length() > 0) {
            config.addAttribute("sheetName", sheetName);
        } else {
            config.addAttribute("sheet", "" + sheetIndex);
        }
        if (type == CELL || type == ROW) {
            config.addAttribute("row", "" + row);
        } else if (type == COLUMN) {
            config.addAttribute("rowStart", "" + row);
        }
        if (type == CELL || type == COLUMN || type == ATTR) {
            config.addAttribute("column", "" + column);
        } else {
            config.addAttribute("columnStart", "" + column);
        }
        Element conditionEl = binding.addElement("condition");
        conditionEl.addAttribute("query", getQueryString());
        Element conditions = binding.addElement("conditions");
        conditions.addAttribute("type", getQueryType().toString());
    }
}

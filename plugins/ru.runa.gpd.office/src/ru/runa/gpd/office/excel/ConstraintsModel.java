package ru.runa.gpd.office.excel;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.util.BackCompatibilityUtils;

public class ConstraintsModel {
    public static final int CELL = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final String CELL_CLASS = "ru.runa.wfe.office.excel.CellConstraints";
    public static final String ROW_CLASS = "ru.runa.wfe.office.excel.RowConstraints";
    public static final String COLUMN_CLASS = "ru.runa.wfe.office.excel.ColumnConstraints";
    public String sheetName = "";
    public int sheetIndex = 1;
    public String variableName = "";
    public final int type;
    public int row = 1;
    public int column = 1;

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

    public ConstraintsModel(int type) {
        this.type = type;
    }

    public static ConstraintsModel deserialize(Element element) {
        ConstraintsModel model;
        String className = element.attributeValue("class");
        className = BackCompatibilityUtils.getClassName(className);
        if (CELL_CLASS.equals(className)) {
            model = new ConstraintsModel(CELL);
        } else if (ROW_CLASS.equals(className)) {
            model = new ConstraintsModel(ROW);
        } else if (COLUMN_CLASS.equals(className)) {
            model = new ConstraintsModel(COLUMN);
        } else {
            throw new RuntimeException("Invaid class '" + className + "'");
        }
        model.variableName = element.attributeValue("variable");
        Element conf = element.element("config");
        model.sheetName = conf.attributeValue("sheetName");
        if (model.sheetName == null || model.sheetName.length() == 0) {
            model.sheetIndex = Integer.parseInt(conf.attributeValue("sheet"));
        }
        if (model.type == CELL || model.type == ROW) {
            model.row = Integer.parseInt(conf.attributeValue("row"));
        } else {
            model.row = Integer.parseInt(conf.attributeValue("rowStart"));
        }
        if (model.type == CELL || model.type == COLUMN) {
            model.column = Integer.parseInt(conf.attributeValue("column"));
        } else {
            model.column = Integer.parseInt(conf.attributeValue("columnStart"));
        }
        return model;
    }

    public void serialize(Document document, Element root) {
        Element el = root.addElement("binding");
        Element conf = el.addElement("config");
        el.addAttribute("variable", variableName);
        switch (type) {
        case CELL:
            el.addAttribute("class", CELL_CLASS);
            break;
        case ROW:
            el.addAttribute("class", ROW_CLASS);
            break;
        case COLUMN:
            el.addAttribute("class", COLUMN_CLASS);
            break;
        }
        if (sheetName != null && sheetName.length() > 0) {
            conf.addAttribute("sheetName", sheetName);
        } else {
            conf.addAttribute("sheet", "" + sheetIndex);
        }
        if (type == CELL || type == ROW) {
            conf.addAttribute("row", "" + row);
        } else {
            conf.addAttribute("rowStart", "" + row);
        }
        if (type == CELL || type == COLUMN) {
            conf.addAttribute("column", "" + column);
        } else {
            conf.addAttribute("columnStart", "" + column);
        }
    }
}

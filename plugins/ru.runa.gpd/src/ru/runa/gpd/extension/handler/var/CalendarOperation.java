package ru.runa.gpd.extension.handler.var;

import java.util.Calendar;
import java.util.Observable;

import org.dom4j.Element;

import com.google.common.base.Strings;

public class CalendarOperation extends Observable {
    public static final String ADD = "+";
    public static final String SET = "=";
    private String fieldName = "";
    private String expression = "";
    private String type = "";
    private boolean businessTime;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String field) {
        this.fieldName = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBusinessTime() {
        return businessTime;
    }

    public void setBusinessTime(boolean businessTime) {
        this.businessTime = businessTime;
    }

    public void serialize(Element parent) {
        Element element = parent.addElement("operation");
        element.addAttribute("type", type);
        element.addAttribute("field", CalendarConfig.CALENDAR_FIELDS.get(fieldName).toString());
        element.addAttribute("expression", expression);
        if (businessTime) {
            element.addAttribute("businessTime", "true");
        }
    }

    public static CalendarOperation deserialize(Element element) {
        CalendarOperation model = new CalendarOperation();
        model.type = element.attributeValue("type");
        String fieldString = element.attributeValue("field");
        if (!Strings.isNullOrEmpty(fieldString)) {
            int field = Integer.parseInt(fieldString);
            if (Calendar.HOUR == field) {
                // back compatibility for
                // https://sourceforge.net/p/runawfe/bugs/1137/
                field = Calendar.HOUR_OF_DAY;
            }
            model.fieldName = CalendarConfig.getFieldName(field);
        }
        model.expression = element.attributeValue("expression");
        if ("true".equals(element.attributeValue("businessTime"))) {
            model.businessTime = true;
        }
        return model;
    }
}

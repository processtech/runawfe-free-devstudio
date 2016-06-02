package ru.runa.gpd.extension.handler.var;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.gpd.util.XmlUtil;

public class CalendarConfig extends Observable {
    private String baseVariableName;
    private final List<CalendarOperation> operations = new ArrayList<CalendarOperation>();
    private String resultVariableName;
    public static final List<String> FIELD_NAMES = new ArrayList<String>();
    public static final List<String> BUSINESS_FIELD_NAMES = new ArrayList<String>();
    public static final Map<String, Integer> CALENDAR_FIELDS = new HashMap<String, Integer>();
    static {
        registerField("YEAR", Calendar.YEAR, false);
        registerField("MONTH", Calendar.MONTH, false);
        registerField("WEEK_OF_YEAR", Calendar.WEEK_OF_YEAR, false);
        registerField("DAY_OF_MONTH", Calendar.DAY_OF_MONTH, false);
        registerField("DAY_OF_YEAR", Calendar.DAY_OF_YEAR, true);
        registerField("HOUR", Calendar.HOUR, false);
        registerField("MINUTE", Calendar.MINUTE, true);
        registerField("SECOND", Calendar.SECOND, false);
    }

    private static void registerField(String fieldName, int field, boolean businessTime) {
        FIELD_NAMES.add(fieldName);
        CALENDAR_FIELDS.put(fieldName, field);
        if (businessTime) {
            BUSINESS_FIELD_NAMES.add(fieldName);
        }
    }

    public static String getFieldName(int field) {
        for (Map.Entry<String, Integer> entry : CALENDAR_FIELDS.entrySet()) {
            if (field == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No mapping for field " + field);
    }

    public CalendarConfig() {
    }

    public CalendarConfig(String xml) {
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        deserializeData(rootElement);
    }

    public String getBaseVariableName() {
        return baseVariableName;
    }

    public void setBaseVariableName(String baseVariableName) {
        this.baseVariableName = baseVariableName;
    }

    public List<CalendarOperation> getOperations() {
        return operations;
    }

    public String getResultVariableName() {
        return resultVariableName;
    }

    public void setResultVariableName(String resultVariableName) {
        this.resultVariableName = resultVariableName;
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    public void deleteOperation(int index) {
        operations.remove(index);
        notifyObservers();
    }

    public void addOperation(String type) {
        CalendarOperation operation = new CalendarOperation();
        operation.setType(type);
        operations.add(operation);
        notifyObservers();
    }

    @Override
    public String toString() {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("calendar");
        serializeData(rootElement);
        return XmlUtil.toString(document);
    }

    protected void serializeData(Element rootElement) {
        if (resultVariableName != null) {
            rootElement.addAttribute("result", resultVariableName);
        }
        if (baseVariableName != null) {
            rootElement.addAttribute("basedOn", baseVariableName);
        }
        for (CalendarOperation operation : operations) {
            operation.serialize(rootElement);
        }
    }

    protected void deserializeData(Element rootElement) {
        this.resultVariableName = rootElement.attributeValue("result");
        this.baseVariableName = rootElement.attributeValue("basedOn");
        List<Element> operationElements = rootElement.elements("operation");
        for (Element operationElement : operationElements) {
            CalendarOperation mapping = CalendarOperation.deserialize(operationElement);
            operations.add(mapping);
        }
    }

}

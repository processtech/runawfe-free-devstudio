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
import org.eclipse.ui.internal.dialogs.TreeManager.ModelListenerForCheckboxTable;

import ru.runa.gpd.util.XmlUtil;

public class DeadlineTransferConfig extends Observable {
	private String processIdVariable;
	private String variableName = "";
	private Boolean isVariableInput = false; //Переменная введена в диалоге
	private final List<CalendarOperation> operations = new ArrayList<CalendarOperation>();
	public static final List<String> FIELD_NAMES = new ArrayList<String>();
    public static final Map<String, Integer> CALENDAR_FIELDS = new HashMap<String, Integer>();
    static {
        registerField("YEAR", Calendar.YEAR);
        registerField("MONTH", Calendar.MONTH);
        registerField("WEEK_OF_YEAR", Calendar.WEEK_OF_YEAR);
        registerField("DAY_OF_MONTH", Calendar.DAY_OF_MONTH);
        registerField("HOUR", Calendar.HOUR);
        registerField("MINUTE", Calendar.MINUTE);
        registerField("SECOND", Calendar.SECOND);
    }
    
    private static void registerField(String fieldName, int field) {
        FIELD_NAMES.add(fieldName);
        CALENDAR_FIELDS.put(fieldName, field);
    }
    
    public static String getFieldName(int field) {
        for (Map.Entry<String, Integer> entry : CALENDAR_FIELDS.entrySet()) {
            if (field == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No mapping for field " + field);
    }

    

    public String getProcessIdVariable() {
		return processIdVariable;
	}

	public void setProcessIdVariable(String processId) {
		this.processIdVariable = processId;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	public Boolean getIsVariableInput() {
		return isVariableInput;
	}

	public void setIsVariableInput(Boolean isVariableInput) {
		this.isVariableInput = isVariableInput;
	}

	public List<CalendarOperation> getOperations() {
        return operations;
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
        if (!processIdVariable.equals("")) {
            rootElement.addAttribute("processId",processIdVariable);
        }
        rootElement.addAttribute("variable", variableName);
        rootElement.addAttribute("is_input", isVariableInput.toString());
        for (CalendarOperation operation : operations) {
            operation.serialize(rootElement);
        }
        return XmlUtil.toString(document);
    }

    public static DeadlineTransferConfig fromXml(String xml) {
    	DeadlineTransferConfig model = new DeadlineTransferConfig();
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element rootElement = document.getRootElement();
        String processIdAttribute = rootElement.attributeValue("processId");
        if (processIdAttribute != null && !processIdAttribute.equals("")) {
        	model.processIdVariable = processIdAttribute;
        }
        model.variableName = rootElement.attributeValue("variable");
        if (model.variableName == null) {
        	model.variableName = "";
        }
        model.isVariableInput = Boolean.valueOf(rootElement.attributeValue("is_input"));
        if (model.isVariableInput == null) {
        	model.isVariableInput = false;
        }
        List<Element> operationElements = rootElement.elements("operation");
        for (Element operationElement : operationElements) {
            CalendarOperation mapping = CalendarOperation.deserialize(operationElement);
            model.operations.add(mapping);
        }
        return model;
    }
}

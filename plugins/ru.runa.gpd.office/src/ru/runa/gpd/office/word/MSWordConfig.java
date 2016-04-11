package ru.runa.gpd.office.word;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.util.XmlUtil;

public class MSWordConfig extends Observable {
    private static final String REPORT = "report";
    private static final String OUTPUT_VARIABLE_FILE_NAME = "output-variable-file-name";
    private static final String OUTPUT_VARIABLE = "output-variable";
    private static final String TEMPLATE_PATH = "template-path";
    private static final String STRICT_MODE = "strict-mode";
    private boolean strictMode = true;
    private String templatePath = "";
    private String resultVariableName = "";
    private String resultFileName = "";
    private final List<MSWordVariableMapping> mappings = new ArrayList<MSWordVariableMapping>();

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getResultVariableName() {
        return resultVariableName;
    }

    public void setResultVariableName(String resultVariableName) {
        this.resultVariableName = resultVariableName;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public List<MSWordVariableMapping> getMappings() {
        return mappings;
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    public void deleteMapping(int index) {
        mappings.remove(index);
        notifyObservers();
    }

    public void addMapping() {
        mappings.add(new MSWordVariableMapping());
        notifyObservers();
    }

    @Override
    public String toString() {
        try {
            Document document = XmlUtil.createDocument("msword-report-task", XmlUtil.RUNA_NAMESPACE, "msword-report-task.xsd");
            Element reportElement = document.getRootElement().addElement(REPORT, XmlUtil.RUNA_NAMESPACE);
            reportElement.addAttribute(STRICT_MODE, String.valueOf(strictMode));
            reportElement.addAttribute(TEMPLATE_PATH, templatePath);
            reportElement.addAttribute(OUTPUT_VARIABLE, resultVariableName);
            reportElement.addAttribute(OUTPUT_VARIABLE_FILE_NAME, resultFileName);
            for (MSWordVariableMapping mapping : mappings) {
                mapping.serialize(reportElement);
            }
            return XmlUtil.toString(document);
        } catch (Exception e) {
            throw new RuntimeException("Unable serialize model to XML", e);
        }
    }

    public static MSWordConfig fromXml(String xml) {
        MSWordConfig model = new MSWordConfig();
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        Element reportElement = root.element(REPORT);
        if (reportElement != null) {
            model.setStrictMode(Boolean.parseBoolean(reportElement.attributeValue(STRICT_MODE, "true")));
            model.templatePath = reportElement.attributeValue(TEMPLATE_PATH);
            model.resultVariableName = reportElement.attributeValue(OUTPUT_VARIABLE);
            model.resultFileName = reportElement.attributeValue(OUTPUT_VARIABLE_FILE_NAME);
            List<Element> mappingElements = reportElement.elements("mapping");
            for (Element mappingElement : mappingElements) {
                MSWordVariableMapping mapping = MSWordVariableMapping.deserialize(mappingElement);
                model.mappings.add(mapping);
            }
        }
        return model;
    }
}

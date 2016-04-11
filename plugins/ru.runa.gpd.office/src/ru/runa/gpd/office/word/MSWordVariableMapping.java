package ru.runa.gpd.office.word;

import java.util.Observable;

import org.dom4j.Element;

import ru.runa.gpd.util.XmlUtil;

public class MSWordVariableMapping extends Observable {
    private String variableName = "";
    private String bookmarkName = "";

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    public void serialize(Element parent) {
        Element element = parent.addElement("mapping", XmlUtil.RUNA_NAMESPACE);
        element.addAttribute("variable", variableName);
        element.addAttribute("bookmark", bookmarkName);
    }

    public static MSWordVariableMapping deserialize(Element element) {
        MSWordVariableMapping model = new MSWordVariableMapping();
        model.variableName = element.attributeValue("variable");
        model.bookmarkName = element.attributeValue("bookmark");
        return model;
    }
}

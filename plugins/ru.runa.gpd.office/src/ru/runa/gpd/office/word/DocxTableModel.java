package ru.runa.gpd.office.word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

public class DocxTableModel extends Observable {
    private static int tableID = 1;
    private String name = "table" + tableID++;
    private String styleName = "";
    private boolean addBreak = true;
    public List<DocxColumnModel> columns = new ArrayList<DocxColumnModel>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public boolean isAddBreak() {
        return addBreak;
    }

    public void setAddBreak(boolean addBreak) {
        this.addBreak = addBreak;
    }

    public void serialize(Document document, Element parent) {
        Element el = parent.addElement("table");
        el.addAttribute("name", name);
        if (styleName.length() > 0) {
            el.addAttribute("styleName", styleName);
        }
        el.addAttribute("addBreak", Boolean.toString(addBreak));
        for (DocxColumnModel model : columns) {
            model.serialize(document, el);
        }
    }

    public static DocxTableModel deserialize(Element element) {
        DocxTableModel model = new DocxTableModel();
        model.name = element.attributeValue("name");
        model.styleName = element.attributeValue("styleName");
        if (model.styleName == null) {
            model.styleName = "";
        }
        model.addBreak = Boolean.parseBoolean(element.attributeValue("addBreak"));
        @SuppressWarnings("unchecked")
        List<Element> queryElements = element.elements("column");
        for (Element qElement : queryElements) {
            DocxColumnModel cModel = DocxColumnModel.deserialize(qElement);
            model.columns.add(cModel);
        }
        return model;
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    public void deleteColumn(int index) {
        columns.remove(index);
        notifyObservers();
    }

    public void addColumn() {
        columns.add(new DocxColumnModel());
        notifyObservers();
    }

    public void moveUpColumn(int index) {
        Collections.swap(columns, index - 1, index);
    }
}

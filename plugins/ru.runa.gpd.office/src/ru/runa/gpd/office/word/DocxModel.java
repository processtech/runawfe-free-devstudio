package ru.runa.gpd.office.word;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputModel;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;

public class DocxModel extends Observable {
    private boolean strict = true;
    private InputOutputModel inOutModel = new InputOutputModel();
    private List<DocxTableModel> tables = new ArrayList<DocxTableModel>();

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }

    public List<DocxTableModel> getTables() {
        return tables;
    }

    public static DocxModel fromXml(String xml) {
        DocxModel model = new DocxModel();
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        model.setStrict(Boolean.parseBoolean(root.attributeValue("strict")));
        Element input = root.element("input");
        Element output = root.element("output");
        model.setInOutModel(InputOutputModel.deserialize(input, output));
        @SuppressWarnings("unchecked")
        List<Element> tableElements = root.elements("table");
        for (Element tableElement : tableElements) {
            DocxTableModel tableModel = DocxTableModel.deserialize(tableElement);
            model.tables.add(tableModel);
        }
        return model;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("config");
        Element root = document.getRootElement();
        root.addAttribute("strict", Boolean.toString(strict));
        getInOutModel().serialize(document, root, FilesSupplierMode.BOTH);
        for (DocxTableModel model : tables) {
            model.serialize(document, root);
        }
        return XmlUtil.toString(document);
    }

    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        for (DocxTableModel tableModel : tables) {
            for (DocxColumnModel columnModel : tableModel.columns) {
                if (Strings.isNullOrEmpty(columnModel.variable)) {
                    errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.docx.table.column.empty")));
                    break;
                }
            }
        }
        getInOutModel().validate(graphElement, FilesSupplierMode.BOTH, errors);
    }

    public void setInOutModel(InputOutputModel inOutModel) {
        this.inOutModel = inOutModel;
    }
}

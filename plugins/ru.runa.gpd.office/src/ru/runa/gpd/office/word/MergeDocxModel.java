package ru.runa.gpd.office.word;

import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.word.DocxModel;
import ru.runa.gpd.util.XmlUtil;

public class MergeDocxModel extends DocxModel {

    public static DocxModel fromXml(String xml, boolean isMultiInput) {
        MergeDocxModel mergeModel = new MergeDocxModel();
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        mergeModel.setStrict(Boolean.parseBoolean(root.attributeValue("strict")));
        @SuppressWarnings("unchecked")
        List<Element> inputList = root.elements("input");
        Element output = root.element("output");
        mergeModel.setInOutModel(MergeInputOutputModel.deserialize(inputList, output));
        return mergeModel;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("config");
        Element root = document.getRootElement();
        root.addAttribute("strict", Boolean.toString(isStrict()));
        ((MergeInputOutputModel) getInOutModel()).serialize(document, root);
        return XmlUtil.toString(document);
    }

    public void validate(GraphElement graphElement, List<ValidationError> errors) {

        getInOutModel().validate(graphElement, FilesSupplierMode.MULTI_IN_SINGLE_OUT, errors);
    }

}

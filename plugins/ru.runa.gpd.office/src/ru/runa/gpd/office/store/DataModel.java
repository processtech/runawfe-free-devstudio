package ru.runa.gpd.office.store;

import com.google.common.base.Strings;
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

public class DataModel extends Observable {
    protected FilesSupplierMode mode;
    protected final InputOutputModel inOutModel;
    public final List<StorageConstraintsModel> constraints = new ArrayList<StorageConstraintsModel>();

    public DataModel(FilesSupplierMode mode) {
        this(mode, new InputOutputModel());
    }

    public DataModel(FilesSupplierMode mode, InputOutputModel inOutModel) {
        this.mode = mode;
        this.inOutModel = inOutModel;
    }

    public InputOutputModel getInOutModel() {
        return inOutModel;
    }

    @SuppressWarnings("unchecked")
    public static DataModel fromXml(String xml, FilesSupplierMode mode) {
        Document document = XmlUtil.parseWithoutValidation(xml);
        Element input = null;
        if (mode.isInSupported()) {
            input = document.getRootElement().element("input");
        }
        Element output = null;
        if (mode.isInSupported()) {
            output = document.getRootElement().element("output");
        }
        InputOutputModel inOutModel = InputOutputModel.deserialize(input, output);
        DataModel model = new DataModel(mode, inOutModel);
        List<Element> constraintsElements = document.getRootElement().elements("binding");
        for (Element constraintsElement : constraintsElements) {
            model.constraints.add(StorageConstraintsModel.deserialize(constraintsElement));
        }
        return model;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("config");
        Element root = document.getRootElement();
        inOutModel.serialize(document, root, mode);
        for (StorageConstraintsModel model : constraints) {
            model.serialize(document, root);
        }
        return XmlUtil.toString(document);
    }

    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        for (StorageConstraintsModel constraintsModel : constraints) {
            if (Strings.isNullOrEmpty(constraintsModel.variableName)) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.xlsx.constraint.variable.empty")));
                break;
            }
        }
        inOutModel.validate(graphElement, mode, errors);
    }
}

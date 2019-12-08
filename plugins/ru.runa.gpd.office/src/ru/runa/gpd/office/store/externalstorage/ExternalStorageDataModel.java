package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.store.DataModel;
import ru.runa.gpd.office.store.QueryType;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.util.XmlUtil;

public class ExternalStorageDataModel extends DataModel {

    public ExternalStorageDataModel(FilesSupplierMode mode) {
        super(mode, new ExternalStorageHandlerInputOutputModel());
    }

    public ExternalStorageDataModel(FilesSupplierMode mode, ExternalStorageHandlerInputOutputModel inOutModel) {
        super(mode, inOutModel);
    }

    @SuppressWarnings("unchecked")
    public static ExternalStorageDataModel fromXml(String xml) {
        final Document document = XmlUtil.parseWithoutValidation(xml);

        final List<StorageConstraintsModel> constraints = (List<StorageConstraintsModel>) document.getRootElement().elements("binding").stream()
                .map(element -> StorageConstraintsModel.deserialize((Element) element)).collect(Collectors.toList());

        Preconditions.checkState(constraints.size() == 1, "Для обработчика внешнего хранилища данных используется только один constraint");

        final StorageConstraintsModel constraintsModel = Iterables.getOnlyElement(constraints);
        final FilesSupplierMode mode = constraintsModel.getQueryType().equals(QueryType.SELECT) ? FilesSupplierMode.BOTH : FilesSupplierMode.IN;

        final ExternalStorageHandlerInputOutputModel inOutModel = ExternalStorageHandlerInputOutputModel.deserialize(
                mode.isInSupported() ? document.getRootElement().element("input") : null,
                mode.isOutSupported() ? document.getRootElement().element("output") : null);

        final ExternalStorageDataModel model = new ExternalStorageDataModel(mode, inOutModel);
        model.constraints.addAll(constraints);
        return model;
    }

    public void setMode(FilesSupplierMode mode) {
        this.mode = mode;
    }

}

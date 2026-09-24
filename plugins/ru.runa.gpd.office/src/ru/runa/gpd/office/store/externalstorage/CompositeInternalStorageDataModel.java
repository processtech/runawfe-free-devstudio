package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import org.dom4j.Document;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.util.XmlUtil;

public class CompositeInternalStorageDataModel extends InternalStorageDataModel {

    public CompositeInternalStorageDataModel(FilesSupplierMode mode) {
        super(mode);
    }

    public CompositeInternalStorageDataModel(FilesSupplierMode mode, ExternalStorageHandlerInputOutputModel inOutModel) {
        super(mode, inOutModel);
    }

    public StorageConstraintsModel getTriggerConstraints() {
        return constraints.get(0);
    }

    public StorageConstraintsModel getExistsConstraints() {
        return constraints.get(1);
    }

    public InternalStorageDataModel getExistsModel() {
        InternalStorageDataModel existsModel = new InternalStorageDataModel(FilesSupplierMode.IN);
        existsModel.constraints.add(getExistsConstraints());
        return existsModel;
    }

    public static CompositeInternalStorageDataModel fromXml(String xml) {
        final Document document = XmlUtil.parseWithoutValidation(xml);

        final List<StorageConstraintsModel> constraints = deserializeConstraints(document, 2);

        final CompositeInternalStorageDataModel model = new CompositeInternalStorageDataModel(
                FilesSupplierMode.BOTH, deserializeInOutModel(document, FilesSupplierMode.BOTH)
        );

        model.constraints.addAll(constraints);
        return model;
    }

    @Override
    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        if (constraints.size() != 2) {
            errors.add(ValidationError.createError(graphElement, "Expected model.constraints.size() == 2, actual " + constraints.size()));
            return;
        }

        final StorageConstraintsModel triggerConstraintsModel = getTriggerConstraints();
        final VariableProvider processVariableProvider = new ProcessDefinitionVariableProvider(graphElement.getProcessDefinition());

        validateConstraints(graphElement, triggerConstraintsModel, processVariableProvider, errors);

        final StorageConstraintsModel existsConstraintsModel = getExistsConstraints();
        final VariableUserType triggerUserType = processVariableProvider.getUserType(triggerConstraintsModel.getSheetName());
        final VariableProvider existsVariableProvider = new SubQueryVariableProvider(processVariableProvider, triggerUserType);

        validateConstraints(graphElement, existsConstraintsModel, existsVariableProvider, errors);
    }

}

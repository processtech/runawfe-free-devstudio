package ru.runa.gpd.office.store.externalstorage;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.DataModel;
import ru.runa.gpd.office.store.QueryType;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.office.store.externalstorage.predicate.PredicateOperationType;
import ru.runa.gpd.util.XmlUtil;

public class InternalStorageDataModel extends DataModel {

    public InternalStorageDataModel(FilesSupplierMode mode) {
        super(mode, new ExternalStorageHandlerInputOutputModel());
    }

    public InternalStorageDataModel(FilesSupplierMode mode, ExternalStorageHandlerInputOutputModel inOutModel) {
        super(mode, inOutModel);
    }

    @SuppressWarnings("unchecked")
    public static InternalStorageDataModel fromXml(String xml) {
        final Document document = XmlUtil.parseWithoutValidation(xml);

        final List<StorageConstraintsModel> constraints = (List<StorageConstraintsModel>) document.getRootElement().elements("binding").stream()
                .map(element -> StorageConstraintsModel.deserialize((Element) element)).collect(Collectors.toList());

        Preconditions.checkState(constraints.size() == 1, "Expected constraints.size() == 1, actual " + constraints.size());

        final StorageConstraintsModel constraintsModel = Iterables.getOnlyElement(constraints);
        final FilesSupplierMode mode = constraintsModel.getQueryType().equals(QueryType.SELECT) ? FilesSupplierMode.BOTH : FilesSupplierMode.IN;

        final ExternalStorageHandlerInputOutputModel inOutModel = ExternalStorageHandlerInputOutputModel.deserialize(
                mode.isInSupported() ? document.getRootElement().element("input") : null,
                mode.isOutSupported() ? document.getRootElement().element("output") : null);

        final InternalStorageDataModel model = new InternalStorageDataModel(mode, inOutModel);
        model.constraints.addAll(constraints);
        return model;
    }

    public void setMode(FilesSupplierMode mode) {
        this.mode = mode;
    }

    @Override
    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        if (constraints.size() != 1) {
            errors.add(ValidationError.createError(graphElement, "Expected model.constraints.size() == 1, actual " + constraints.size()));
            return;
        }

        final StorageConstraintsModel constraintsModel = Iterables.getOnlyElement(constraints);
        if (constraintsModel.getQueryType() == QueryType.INSERT || constraintsModel.getQueryType() == QueryType.UPDATE) {
            super.validate(graphElement, errors);
            if (!errors.isEmpty()) {
                return;
            }
        } else {
            inOutModel.validate(graphElement, mode, errors);
        }

        final String queryString = constraintsModel.getQueryString();
        if (queryString == null || queryString.trim().isEmpty()) {
            return;
        }

        final String pattern = PredicateOperationType.codes().stream()
                .filter(code -> !code.equals(PredicateOperationType.AND.code) && !code.equals(PredicateOperationType.OR.code))
                .collect(Collectors.joining("|", "(", ")"));
        try (Scanner expressionScanner = new Scanner(queryString)
                .useDelimiter("(" + PredicateOperationType.AND.code + "|" + PredicateOperationType.OR.code + ")")) {
            while (expressionScanner.hasNext()) {
                try (Scanner variableScanner = new Scanner(expressionScanner.next()).useDelimiter(pattern)) {
                    while (variableScanner.hasNext()) {
                        final String candidate = variableScanner.next();
                        final int i = candidate.indexOf("@");
                        if (i == -1) {
                            continue;
                        }
                        validateVariableHasDefaultValue(graphElement, errors, candidate.substring(i + 1).trim());
                    }
                }
            }
        }
    }

    private void validateVariableHasDefaultValue(GraphElement graphElement, List<ValidationError> errors, String variableName) {
        final Optional<Variable> searchResult = graphElement.getProcessDefinition().getVariables(true, false).stream()
                .filter(variable -> Objects.equal(variableName, variable.getName())).findAny();
        if (searchResult.isPresent() && searchResult.get().getDefaultValue() == null) {
            errors.add(ValidationError.createError(graphElement,
                    MessageFormat.format(Messages.getString("model.validation.constraint.variable.nodefaultvalue"), variableName)));
        }
    }

}

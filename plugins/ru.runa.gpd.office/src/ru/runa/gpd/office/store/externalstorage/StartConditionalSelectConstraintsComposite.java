package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.store.InternalStorageOperationHandlerCellEditorProvider;
import ru.runa.gpd.office.store.StorageConstraintsModel;

/**
 * {@link SelectConstraintsComposite} для BPMN-события <b>Conditional Start Event</b>
 * (стартовое событие по данным из внутреннего хранилища).
 * <p>
 * В отличие от базовой реализации, позволяет выбрать в качестве {@code outputVariable}
 * не только переменную-список, но и одиночную переменную того же типа. Это связано
 * с тем, что при старте процесса по условию в него передаётся <b>одна</b> запись
 * из списка найденных — поэтому пользователю нужно уметь указать скалярную переменную,
 * а не только список.
 * <p>
 */
public class StartConditionalSelectConstraintsComposite extends SelectConstraintsComposite {

    public StartConditionalSelectConstraintsComposite(
            Composite parent,
            int style,
            StorageConstraintsModel constraintsModel,
            VariableProvider variableProvider,
            InternalStorageOperationHandlerCellEditorProvider.VariableUserTypeInfo variableUserTypeInfo,
            InternalStorageDataModel dataModel) {
        super(parent, style, constraintsModel, variableProvider, variableUserTypeInfo, dataModel);
    }

    @Override
    protected Stream<String> variableNamesByVariableTypeName(String variableTypeName) {
        Stream<String> listVariablesStream = variableProvider
                .getVariables(true, false, List.class.getName()).stream()
                .filter(variable -> variable.getFormatComponentClassNames()[0].equals(variableTypeName))
                .map(Variable::getName);
        Stream<String> singleVariablesStream = variableProvider
                .getVariables(true, false, variableTypeName).stream().map(Variable::getName);
        return Stream.concat(listVariablesStream, singleVariablesStream);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate(String variableTypeName) {
        throw new UnsupportedOperationException("variableNamesByVariableTypeName is overridden here");
    }

}

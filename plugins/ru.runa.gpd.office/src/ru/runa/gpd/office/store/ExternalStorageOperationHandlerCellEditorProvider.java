package ru.runa.gpd.office.store;

import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.wfe.var.UserTypeMap;

public class ExternalStorageOperationHandlerCellEditorProvider extends BaseCommonStorageHandlerCellEditorProvider {

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.IN;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("ExternalStorageHandlerConfig.title");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, DataModel model) {
        return new ConstructorView(parent, delegable, model);
    }

    private class ConstructorView extends ConstructorComposite {
        private static final String INTERNAL_STORAGE_DATASOURCE_PATH = "datasource:InternalStorage";
        private StorageConstraintsModel constraintsModel;
        private String variableTypeName;

        private ConstraintsCompositeBuilder constraintsCompositeBuilder;

        public ConstructorView(Composite parent, Delegable delegable, DataModel model) {
            super(parent, delegable, model);
            model.getInOutModel().inputPath = INTERNAL_STORAGE_DATASOURCE_PATH;
            setLayout(new GridLayout(2, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            initConstraintsModel();
            for (Control control : getChildren()) {
                control.dispose();
            }

            if (constraintsModel.getVariableName() != null && !constraintsModel.getVariableName().isEmpty()) {
                variableTypeName = getVariableTypeNameByVariableName((VariableContainer) delegable, constraintsModel.getVariableName());
            }
            new Label(this, SWT.NONE).setText(Messages.getString("label.ExecutionAction"));
            addActionCombo();

            new Label(this, SWT.NONE).setText(Messages.getString("label.DataType"));
            addDataTypeCombo();

            initConstraintsCompositeBuilder();
            if (constraintsCompositeBuilder != null) {
                constraintsCompositeBuilder.build();
            }

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            this.layout(true, true);
            this.redraw();
        }

        private String getVariableTypeNameByVariableName(VariableContainer variableContainer, String variableName) {
            return variableContainer.getVariables(false, false, UserTypeMap.class.getName()).stream()
                    .filter(variable -> variable.getName().equals(variableName)).map(variable -> variable.getUserType().getName()).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Не найден тип для переменной " + variableName));
        }

        private void initConstraintsModel() {
            if (constraintsModel != null && model.constraints.get(0) == constraintsModel) {
                return;
            }

            if (!model.constraints.isEmpty()) {
                Preconditions.checkState(model.constraints.size() == 1,
                        "Для обработчика внешнего хранилища данных используется только один constraint");
                constraintsModel = Iterables.getOnlyElement(model.constraints);
            } else {
                constraintsModel = new StorageConstraintsModel(StorageConstraintsModel.ATTR, QueryType.SELECT);
                model.constraints.add(constraintsModel);
            }
        }

        private void initConstraintsCompositeBuilder() {
            if (constraintsModel.getQueryType() != null) {
                switch (constraintsModel.getQueryType()) {
                case INSERT:
                    constraintsCompositeBuilder = new InsertConstraintsComposite(this, SWT.NONE, constraintsModel, (VariableContainer) delegable,
                            variableTypeName);
                    break;
                case DELETE:
                case SELECT:
                case UPDATE:
                    constraintsCompositeBuilder = new ConstraintsCompositeStub(this, SWT.NONE, constraintsModel);
                    break;
                }
            }
        }

        private void addDataTypeCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            ((VariableContainer) delegable).getVariables(false, false, UserTypeMap.class.getName()).stream()
                    .map(variable -> variable.getUserType().getName()).collect(Collectors.toSet()).forEach(variableTypeName -> {
                        combo.add(variableTypeName);
                    });
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    final String text = combo.getText();
                    if (Strings.isNullOrEmpty(text)) {
                        return;
                    }
                    variableTypeName = text;
                    if (constraintsCompositeBuilder != null) {
                        constraintsCompositeBuilder.onChangeVariableTypeName(variableTypeName);
                    }
                }
            });

            if (variableTypeName != null) {
                combo.setText(variableTypeName);
            }
        }

        private void addActionCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            for (QueryType action : QueryType.values()) {
                combo.add(action.toString());
            }
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String text = combo.getText();
                    if (Strings.isNullOrEmpty(text)) {
                        return;
                    }
                    constraintsModel.setQueryType(QueryType.valueOf(combo.getText()));
                    buildFromModel();
                    if (constraintsCompositeBuilder != null) {
                        constraintsCompositeBuilder.clearConstraints();
                    }
                }
            });

            combo.setText(constraintsModel.getQueryType() != null ? constraintsModel.getQueryType().toString() : QueryType.SELECT.toString());
        }
    }

    private static interface ConstraintsCompositeBuilder {
        void build();

        void onChangeVariableTypeName(String variableTypeName);

        void clearConstraints();
    }

    private static class InsertConstraintsComposite extends Composite implements ConstraintsCompositeBuilder {
        private final StorageConstraintsModel constraintsModel;
        private final VariableContainer variableContainer;
        private String variableTypeName;
        private Combo combo;

        public InsertConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableContainer variableContainer,
                String variableTypeName) {
            super(parent, style);
            setLayout(new GridLayout(2, false));
            this.constraintsModel = constraintsModel;
            this.variableContainer = variableContainer;
            this.variableTypeName = variableTypeName;
        }

        @Override
        public void onChangeVariableTypeName(String variableTypeName) {
            this.variableTypeName = variableTypeName;
            combo.removeAll();
            combo.setText("");
            constraintsModel.setVariableName("");
            getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);
        }

        @Override
        public void build() {
            new Label(this, SWT.NONE).setText(Messages.getString("label.InsertVariable"));
            addInsertVariableCombo();
        }

        @Override
        public void clearConstraints() {
            constraintsModel.setVariableName("");
            constraintsModel.setQueryString("");
        }

        private void addInsertVariableCombo() {
            combo = new Combo(this, SWT.READ_ONLY); // TODO #1506 Реализовать адаптивный чекбокс
            getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);

            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    final String text = combo.getText();
                    if (Strings.isNullOrEmpty(text)) {
                        return;
                    }
                    constraintsModel.setVariableName(text);
                }
            });
            if (constraintsModel.getVariableName() != null) {
                combo.setText(constraintsModel.getVariableName());
            }
        }

        private Iterable<String> getVariableNamesByVariableTypeName(String variableTypeName) {
            return variableContainer.getVariables(false, false, UserTypeMap.class.getName()).stream()
                    .filter(variable -> variable.getUserType().getName().equals(variableTypeName)).map(Variable::getName).collect(Collectors.toSet());
        }

    }

    private static class ConstraintsCompositeStub extends Composite implements ConstraintsCompositeBuilder {

        private final StorageConstraintsModel constraintsModel;

        public ConstraintsCompositeStub(Composite parent, int style, StorageConstraintsModel constraintsModel) {
            super(parent, style);
            this.constraintsModel = constraintsModel;
        }

        @Override
        public void onChangeVariableTypeName(String variableTypeName) {
            // TODO Auto-generated method stub

        }

        @Override
        public void build() {
            // TODO Auto-generated method stub

        }

        @Override
        public void clearConstraints() {
            constraintsModel.setVariableName("");
            constraintsModel.setQueryString("");
        }

    }
}

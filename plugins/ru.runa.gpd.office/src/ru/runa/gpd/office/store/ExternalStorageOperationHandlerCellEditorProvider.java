package ru.runa.gpd.office.store;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.externalstorage.ConstraintsCompositeBuilder;
import ru.runa.gpd.office.store.externalstorage.DeleteConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.ExternalStorageDataModel;
import ru.runa.gpd.office.store.externalstorage.InsertConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.PredicateCompositeDelegateBuilder;
import ru.runa.gpd.office.store.externalstorage.ProcessDefinitionVariableProvider;
import ru.runa.gpd.office.store.externalstorage.SelectConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.UpdateConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.util.EmbeddedFileUtils;

public class ExternalStorageOperationHandlerCellEditorProvider extends XmlBasedConstructorProvider<ExternalStorageDataModel> {
    @Override
    public void onDelete(Delegable delegable) {
        try {
            final ExternalStorageDataModel model = fromXml(delegable.getDelegationConfiguration());
            EmbeddedFileUtils.deleteProcessFile(model.getInOutModel().inputPath);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Template file deletion", e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getString("ExternalStorageHandlerConfig.title");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, ExternalStorageDataModel model) {
        if (delegable instanceof VariableContainer) {
            final ProcessDefinition processDefinition = ((VariableContainer) delegable).getVariables(false, true).stream()
                    .map(variable -> variable.getProcessDefinition()).findAny()
                    .orElseThrow(() -> new IllegalStateException("process definition unavailable"));
            return new ConstructorView(parent, delegable, model, new ProcessDefinitionVariableProvider(processDefinition));
        } else {
            // TODO 1506 Реализовать VariableProvider для параметров бота
            throw new UnsupportedOperationException("Не реализован VariableProvider для " + delegable.getClass().getName());
        }
    }

    @Override
    protected ExternalStorageDataModel createDefault() {
        return new ExternalStorageDataModel(FilesSupplierMode.BOTH);
    }

    @Override
    protected ExternalStorageDataModel fromXml(String xml) throws Exception {
        return ExternalStorageDataModel.fromXml(xml);
    }

    @Override
    protected boolean validateModel(Delegable delegable, ExternalStorageDataModel model, List<ValidationError> errors) {
        GraphElement graphElement = ((GraphElement) delegable);
        model.validate(graphElement, errors);
        return super.validateModel(delegable, model, errors);
    }

    private class ConstructorView extends ConstructorComposite {
        private static final String INTERNAL_STORAGE_DATASOURCE_PATH = "datasource:InternalStorage";

        private final VariableProvider variableProvider;

        private StorageConstraintsModel constraintsModel;
        private String variableTypeName;

        private ConstraintsCompositeBuilder constraintsCompositeBuilder;

        public ConstructorView(Composite parent, Delegable delegable, ExternalStorageDataModel model, VariableProvider variableProvider) {
            super(parent, delegable, model);
            this.variableProvider = variableProvider;
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
                final VariableUserType userType = variableProvider.getUserType(constraintsModel.getSheetName());
                variableTypeName = userType != null ? userType.getName() : "";
                constraintsModel.setSheetName(variableTypeName);
            }
            new Label(this, SWT.NONE).setText(Messages.getString("label.ExecutionAction"));
            addActionCombo();

            new Label(this, SWT.NONE).setText(Messages.getString("label.DataType"));
            addDataTypeCombo();

            initConstraintsCompositeBuilder();
            if (constraintsCompositeBuilder != null) {
                new Label(this, SWT.NONE);
                constraintsCompositeBuilder.build();
            }

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            this.layout(true, true);
            this.redraw();
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
                    constraintsCompositeBuilder = new InsertConstraintsComposite(this, SWT.NONE, constraintsModel, variableProvider,
                            variableTypeName);
                    break;
                case SELECT:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableTypeName, new SelectConstraintsComposite(this, SWT.NONE, constraintsModel, variableProvider, variableTypeName,
                                    (resultVariableName) -> model.getInOutModel().outputVariable = resultVariableName));
                    break;
                case UPDATE:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableTypeName, new UpdateConstraintsComposite(this, SWT.NONE, constraintsModel, variableProvider, variableTypeName));
                    break;
                case DELETE:
                    constraintsCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, constraintsModel, variableProvider,
                            variableTypeName, new DeleteConstraintsComposite(this, SWT.NONE, constraintsModel, variableProvider, variableTypeName));
                    break;
                }
            }
        }

        private void addDataTypeCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            variableProvider.complexUserTypeNames().collect(Collectors.toSet()).forEach(combo::add);
            combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                final String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                variableTypeName = text;
                constraintsModel.setSheetName(variableTypeName);
                if (constraintsCompositeBuilder != null) {
                    constraintsCompositeBuilder.onChangeVariableTypeName(variableTypeName);
                }
                buildFromModel();
            }));

            final VariableUserType userType = variableProvider.getUserType(constraintsModel.getSheetName());
            if (userType != null) {
                combo.setText(userType.getName());
                variableTypeName = userType.getName();
            }
        }

        private void addActionCombo() {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            for (QueryType action : QueryType.values()) {
                combo.add(action.toString());
            }
            combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                constraintsModel.setQueryType(QueryType.valueOf(combo.getText()));
                model.setMode(constraintsModel.getQueryType().equals(QueryType.SELECT) ? FilesSupplierMode.BOTH : FilesSupplierMode.IN);
                model.getInOutModel().outputVariable = null;
                buildFromModel();
                if (constraintsCompositeBuilder != null) {
                    constraintsCompositeBuilder.clearConstraints();
                }
            }));

            combo.setText(constraintsModel.getQueryType() != null ? constraintsModel.getQueryType().toString() : QueryType.SELECT.toString());
        }
    }
}

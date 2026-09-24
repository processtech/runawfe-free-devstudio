package ru.runa.gpd.office.store;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.externalstorage.CompositeInternalStorageDataModel;
import ru.runa.gpd.office.store.externalstorage.ConstraintsCompositeBuilder;
import ru.runa.gpd.office.store.externalstorage.InternalStorageDataModel;
import ru.runa.gpd.office.store.externalstorage.PredicateCompositeDelegateBuilder;
import ru.runa.gpd.office.store.externalstorage.ProcessDefinitionVariableProvider;
import ru.runa.gpd.office.store.externalstorage.QueryRole;
import ru.runa.gpd.office.store.externalstorage.SelectConstraintsComposite;
import ru.runa.gpd.office.store.externalstorage.SubQueryVariableProvider;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.ui.control.IntervalControl;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.enhancement.ConditionalEventStorageDelegableAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.util.XmlUtil;

import static ru.runa.gpd.ui.enhancement.ConditionalEventDelegableAdapters.adaptToStorage;

public class StartInternalStorageOperationHandlerCellEditorProvider extends InternalStorageOperationHandlerCellEditorProvider {


    @Override
    protected Point getDialogInitialSize() {
        return new Point(800, 800);
    }

    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode mode) {
        StartInternalStorageStorageDialog dialog = new StartInternalStorageStorageDialog(adaptToStorage(delegable));
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }


    @Override
    public Object showEmbeddedConfigurationDialog(final Composite mainComposite, Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        return super.showEmbeddedConfigurationDialog(mainComposite, adaptToStorage(delegable), dialogEnhancementMode);
    }


    @Override
    public void onDelete(Delegable delegable) {
        super.onDelete(adaptToStorage(delegable));
    }


    @Override
    protected InternalStorageDataModel createDefault() {
        CompositeInternalStorageDataModel model = new CompositeInternalStorageDataModel(FilesSupplierMode.BOTH);
        model.constraints.add(new StorageConstraintsModel(StorageConstraintsModel.ATTR, QueryType.SELECT, QueryRole.TRIGGER));
        model.constraints.add(new StorageConstraintsModel(StorageConstraintsModel.ATTR, QueryType.SELECT, QueryRole.NOT_EXISTS));
        return model;
    }


    @Override
    protected InternalStorageDataModel fromXml(String xml) {
        return CompositeInternalStorageDataModel.fromXml(xml);
    }


    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, InternalStorageDataModel model) {

        StorageAware storageAware = (StorageAware) delegable;

        Optional<ProcessDefinition> processDefinition = Optional.ofNullable(((ProcessDefinitionAware) delegable).getProcessDefinition());

        if (!processDefinition.isPresent()) {
            processDefinition = ((VariableContainer) delegable)
                    .getVariables(false, true)
                    .stream()
                    .map(GraphElement::getProcessDefinition)
                    .findAny();
        }

        VariableProvider provider = new ProcessDefinitionVariableProvider(
                processDefinition.orElseThrow(() -> new IllegalStateException("process definition unavailable"))
        );

        return new StartInternalStorageConstructorView(
                parent,
                delegable,
                model,
                provider,
                storageAware.isUseExternalStorageIn(),
                storageAware.isUseExternalStorageOut()
        ).build();
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) throws Exception {
        return super.validateValue(adaptToStorage(delegable), errors);
    }

    @Override
    protected boolean validateModel(Delegable delegable, InternalStorageDataModel model, List<ValidationError> errors) {
        final GraphElement graphElement = ((GraphElementAware) delegable).getGraphElement();
        model.validate(graphElement, errors);
        return true;
    }

    protected class StartInternalStorageConstructorView extends ConstructorView {

        private final VariableUserTypeInfo triggerVariableUserTypeInfo = new VariableUserTypeInfo(false, "");
        private final VariableUserTypeInfo existsVariableUserTypeInfo = new VariableUserTypeInfo(false, "");

        private final ConditionalEventModel conditionalEventModel;
        private StorageConstraintsModel triggerConstraintsModel;
        private StorageConstraintsModel existsConstraintsModel;
        private ConstraintsCompositeBuilder triggerCompositeBuilder;
        private ConstraintsCompositeBuilder existsCompositeBuilder;


        public StartInternalStorageConstructorView(
                Composite parent,
                Delegable delegable,
                InternalStorageDataModel model,
                VariableProvider variableProvider,
                boolean isUseExternalStorageIn,
                boolean isUseExternalStorageOut) {

            super(
                    parent,
                    delegable,
                    model,
                    variableProvider,
                    isUseExternalStorageIn,
                    isUseExternalStorageOut,
                    new VariableUserTypeInfo(false, "")
            );
            this.conditionalEventModel = ((ConditionalEventStorageDelegableAdapter<?>) delegable).getModel();
            model.getInOutModel().inputPath = InternalStorageOperationHandlerCellEditorProvider.INTERNAL_STORAGE_DATASOURCE_PATH;
            setLayout(new GridLayout(2, false));
        }


        @Override
        protected void buildFromModel() {

            initConstraintsModel();

            for (Control control : getChildren()) {
                control.dispose();
            }

            new Label(this, SWT.NONE).setText(Messages.getString("label.ExecutionAction"));
            SwtUtils.createLabel(this, QueryType.SELECT.name());
            triggerConstraintsModel.setQueryType(QueryType.SELECT);
            existsConstraintsModel.setQueryType(QueryType.SELECT);
            model.setMode(FilesSupplierMode.BOTH);

            new IntervalControl(
                    this,
                    conditionalEventModel.getInterval(),
                    conditionalEventModel::setInterval,
                    new GridData(SWT.LEFT, SWT.CENTER, false, false)
            );

            new Label(this, SWT.NONE).setText(Messages.getString("label.TriggerDataType"));
            final String previousTriggerType = triggerVariableUserTypeInfo.getVariableTypeName();
            addDataTypeCombo(triggerConstraintsModel, triggerVariableUserTypeInfo, () -> {
                if (!triggerVariableUserTypeInfo.getVariableTypeName().equals(previousTriggerType)) {
                    existsConstraintsModel.setQueryString("");
                }
            });
            initTriggerCompositeBuilder();
            triggerCompositeBuilder.clearConstraints();
            new Label(this, SWT.NONE);
            triggerCompositeBuilder.build();

            new Label(this, SWT.NONE).setText(Messages.getString("label.ExistsDataType"));
            addDataTypeCombo(existsConstraintsModel, existsVariableUserTypeInfo, () -> {
            });
            initExistsCompositeBuilder();
            existsCompositeBuilder.clearConstraints();
            new Label(this, SWT.NONE);
            existsCompositeBuilder.build();

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            layout(true, true);
            redraw();
        }


        @Override
        protected void initConstraintsModel() {
            CompositeInternalStorageDataModel startModel = (CompositeInternalStorageDataModel) model;
            if (constraintsModel == startModel.getTriggerConstraints()
                    && existsConstraintsModel == startModel.getExistsConstraints()) {
                return;
            }
            triggerConstraintsModel = startModel.getTriggerConstraints();
            existsConstraintsModel = startModel.getExistsConstraints();
        }

        private void initTriggerCompositeBuilder() {
            triggerConstraintsModel.setQueryType(QueryType.SELECT);
            triggerCompositeBuilder = new PredicateCompositeDelegateBuilder(this, SWT.NONE, triggerConstraintsModel, variableProvider,
                    triggerVariableUserTypeInfo.getVariableTypeName(), new SelectConstraintsComposite(this, SWT.NONE, triggerConstraintsModel,
                    variableProvider, triggerVariableUserTypeInfo, model));
        }

        private void initExistsCompositeBuilder() {
            existsConstraintsModel.setQueryType(QueryType.SELECT);
            VariableUserType triggerUserType = variableProvider.getUserType(triggerVariableUserTypeInfo.getVariableTypeName());
            VariableProvider existsVariableProvider = triggerUserType != null
                    ? new SubQueryVariableProvider(variableProvider, triggerUserType)
                    : variableProvider;
            existsCompositeBuilder = new PredicateCompositeDelegateBuilder(
                    this, SWT.NONE, existsConstraintsModel, existsVariableProvider,
                    existsVariableUserTypeInfo.getVariableTypeName(),
                    new SelectConstraintsComposite(
                            this,
                            SWT.NONE,
                            existsConstraintsModel,
                            existsVariableProvider,
                            existsVariableUserTypeInfo,
                            ((CompositeInternalStorageDataModel) model).getExistsModel()
                    ));
        }

        protected void addDataTypeCombo(StorageConstraintsModel targetConstraintsModel, VariableUserTypeInfo targetVariableUserTypeInfo,
                Runnable onTableChanged) {
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            variableProvider.complexUserTypeNames().collect(Collectors.toSet()).forEach(combo::add);
            combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
                final String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                targetVariableUserTypeInfo.setVariableTypeName(text);
                targetConstraintsModel.setSheetName(text);
                onTableChanged.run();
                buildFromModel();
            }));
            final VariableUserType userType = variableProvider.getUserType(targetConstraintsModel.getSheetName());
            if (userType != null) {
                combo.setText(userType.getName());
                targetVariableUserTypeInfo.setVariableTypeName(userType.getName());
            }
        }
    }

    protected class StartInternalStorageStorageDialog extends XmlBasedConstructorDialog {

        private final ConditionalEventModel conditionalEventModel;

        public StartInternalStorageStorageDialog(ConditionalEventStorageDelegableAdapter<?> delegable) {
            super(delegable);
            this.conditionalEventModel = delegable.getModel();
        }


        @Override
        protected void populateToSourceView() {
            Element storage = model.toDocument().getRootElement();
            conditionalEventModel.setStorage(storage);

            Document document = XmlUtil.createDocument(conditionalEventModel.getStorage());
            xmlContentView.setValue(XmlUtil.toString(document));
        }


        @Override
        protected void okPressed() {
            if (tabFolder.getSelectionIndex() == 1) {
                try {
                    conditionalEventModel.setStorage(XmlUtil.parseWithoutValidation(xmlContentView.getValue()).getRootElement());
                } catch (Exception e) {
                    PluginLogger.logError("Unable to parse model from XML", e);
                }
            }
            super.okPressed();
        }

        @Override
        public String getResult() {
            return conditionalEventModel.toXml();
        }
    }

}

package ru.runa.gpd.office.store;

import java.util.List;
import java.util.Optional;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.ltk.ConditionalEventDelegablePresentation;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.externalstorage.InternalStorageDataModel;
import ru.runa.gpd.office.store.externalstorage.ProcessDefinitionVariableProvider;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.ui.control.IntervalControl;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.enhancement.ConditionalEventStorageDelegableAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.util.XmlUtil;

/**
 * Provider for conditional event storage configuration.
 * <p>
 * This class does not provide custom variable rename refactoring.
 * Variable rename refactoring is implemented in
 * {@link ConditionalEventDelegablePresentation}.
 */
public class ConditionalEventInternalStorageOperationHandlerCellEditorProvider extends InternalStorageOperationHandlerCellEditorProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode mode) {
        ConditionalEventStorageDialog dialog = new ConditionalEventStorageDialog(adapt(delegable));
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public Object showEmbeddedConfigurationDialog(final Composite mainComposite, Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        return super.showEmbeddedConfigurationDialog(mainComposite, adapt(delegable), dialogEnhancementMode);
    }

    @Override
    public void onDelete(Delegable delegable) {
        super.onDelete(adapt(delegable));
    }

    /**
     * Invoked during UI construction of {@link ConditionalEventStorageDialog}
     * by the inherited {@code createDialogArea(Composite)} implementation
     * in {@link XmlBasedConstructorDialog}.
     */
    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, InternalStorageDataModel model) {
        final boolean isUseExternalStorageIn = ((StorageAware) delegable).isUseExternalStorageIn();
        final boolean isUseExternalStorageOut = ((StorageAware) delegable).isUseExternalStorageOut();

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

        return new ConditionalEventConstructorView(
                parent,
                delegable,
                model,
                provider,
                isUseExternalStorageIn,
                isUseExternalStorageOut
        ).build();
    }

    @Override
    protected InternalStorageDataModel createDefault() {
        InternalStorageDataModel model = new InternalStorageDataModel(FilesSupplierMode.IN);
        model.constraints.add(new StorageConstraintsModel(StorageConstraintsModel.ATTR, QueryType.SELECT));
        return model;
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) throws Exception {
        return super.validateValue(adapt(delegable), errors);
    }

    protected ConditionalEventStorageDelegableAdapter adapt(Delegable delegable) {
        return new ConditionalEventStorageDelegableAdapter((CatchEventNode) delegable);
    }

    protected class ConditionalEventConstructorView extends ConstructorView {

        private final ConditionalEventModel conditionalEventModel;

        public ConditionalEventConstructorView(Composite parent, Delegable delegable, InternalStorageDataModel model, VariableProvider variableProvider, boolean isUseExternalStorageIn, boolean isUseExternalStorageOut) {
            super(parent, delegable, model, variableProvider, isUseExternalStorageIn, isUseExternalStorageOut, new VariableUserTypeInfo(false, ""));
            this.conditionalEventModel = ((ConditionalEventStorageDelegableAdapter) delegable).getModel();
        }

        @Override
        protected void buildFromModel() {
            initConstraintsModel();

            for (Control control : getChildren()) {
                control.dispose();
            }

            new Label(this, SWT.NONE).setText(Messages.getString("label.ExecutionAction"));
            SwtUtils.createLabel(this, QueryType.SELECT.name());
            constraintsModel.setQueryType(QueryType.SELECT);
            model.setMode(FilesSupplierMode.IN);

            new Label(this, SWT.NONE).setText(Messages.getString("label.DataType"));
            addDataTypeCombo();

            new IntervalControl(
                    this,
                    conditionalEventModel.getInterval(),
                    conditionalEventModel::setInterval,
                    new GridData(SWT.LEFT, SWT.CENTER, false, false)
            );

            initConstraintsCompositeBuilder();
            if (constraintsCompositeBuilder != null) {
                constraintsCompositeBuilder.clearConstraints();
                new Label(this, SWT.NONE);
                constraintsCompositeBuilder.build();
            }

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            layout(true, true);
            redraw();
        }
    }

    protected class ConditionalEventStorageDialog extends XmlBasedConstructorDialog {

        private final ConditionalEventModel conditionalEventModel;

        /**
         * The {@code delegable} instance passed from
         * {@link #createConstructorComposite(Composite, Delegable, InternalStorageDataModel)},
         * which is invoked by {@link ConditionalEventStorageDialog#createDialogArea(Composite)}.
         * Expected to be a {@link ConditionalEventStorageDelegableAdapter}.
         */
        public ConditionalEventStorageDialog(Delegable delegable) {
            super(delegable);
            this.conditionalEventModel = ((ConditionalEventStorageDelegableAdapter) delegable).getModel();
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

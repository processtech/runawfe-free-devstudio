package ru.runa.gpd.extension.handler.var;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class SetDateVariableHandlerProvider<T extends CalendarConfig> extends XmlBasedConstructorProvider<CalendarConfig> {
    private static String[] DATE_TYPES = new String[] { Date.class.getName() };

    @Override
    protected CalendarConfig createDefault() {
        return new CalendarConfig();
    }

    @Override
    protected CalendarConfig fromXml(String xml) {
        return new CalendarConfig(xml);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, CalendarConfig config) {
        return new ConstructorView(parent, delegable, config);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("ru.runa.wfe.extension.handler.var.CreateCalendarHandler");
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) {
        List<String> result = Lists.newArrayList();
        CalendarConfig model = fromXml(delegable.getDelegationConfiguration());
        if (model != null) {
            fillUserVariableNames(result, (T) model);
        }
        return result;
    }

    protected void fillUserVariableNames(List<String> result, T model) {
        if (!Strings.isNullOrEmpty(model.getBaseVariableName())) {
            result.add(model.getBaseVariableName());
        }
        if (!Strings.isNullOrEmpty(model.getResultVariableName())) {
            result.add(model.getResultVariableName());
        }
    }

    @Override
    protected boolean validateModel(Delegable delegable, CalendarConfig model, List<ValidationError> errors) {
        if (!validateResultVariable(delegable, model.getResultVariableName())) {
            return false;
        }
        for (CalendarOperation operation : model.getOperations()) {
            if (Strings.isNullOrEmpty(operation.getExpression())) {
                return false;
            }
            if (operation.isBusinessTime() && !CalendarConfig.BUSINESS_FIELD_NAMES.contains(operation.getFieldName())) {
                errors.add(ValidationError.createLocalizedError((GraphElement) delegable, "delegable.calendar.businesstime.error"));
            }
        }
        return super.validateModel(delegable, model, errors);
    }

    protected boolean validateResultVariable(Delegable delegable, String resultVariableName) {
        if (Strings.isNullOrEmpty(resultVariableName)) {
            return false;
        }
        if (!delegable.getVariableNames(false, DATE_TYPES).contains(resultVariableName)) {
            return false;
        }
        return true;
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        CalendarConfig model = fromXml(delegable.getDelegationConfiguration());
        if (model != null) {
            applyConfigurationOnVariableRename(model, currentVariable, previewVariable);
        }
        return model.toString();
    }

    protected void applyConfigurationOnVariableRename(CalendarConfig model, Variable currentVariable, Variable previewVariable) {
        if (Objects.equal(model.getBaseVariableName(), currentVariable.getName())) {
            model.setBaseVariableName(previewVariable.getName());
        }
        if (Objects.equal(model.getResultVariableName(), currentVariable.getName())) {
            model.setResultVariableName(previewVariable.getName());
        }
    }

    protected class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, Delegable delegable, CalendarConfig config) {
            super(parent, delegable, config);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                addRootSection(true);
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        protected GridData get2GridData() {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            return data;
        }

        protected void addRootSection(boolean addResultVariableSection) {
            if (addResultVariableSection) {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("ParamBasedProvider.result"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(false, DATE_TYPES)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                if (model.getResultVariableName() != null) {
                    combo.setText(model.getResultVariableName());
                }
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    public void onSelection(SelectionEvent e) {
                        model.setResultVariableName(combo.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("property.duration.baseDate"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Duration.CURRENT_DATE_MESSAGE);
                for (String variableName : delegable.getVariableNames(false, DATE_TYPES)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                if (model.getBaseVariableName() != null) {
                    combo.setText(model.getBaseVariableName());
                } else {
                    combo.setText(Duration.CURRENT_DATE_MESSAGE);
                }
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    public void onSelection(SelectionEvent e) {
                        model.setBaseVariableName(combo.getText());
                        if (Duration.CURRENT_DATE_MESSAGE.equals(model.getBaseVariableName())) {
                            model.setBaseVariableName(null);
                        }
                    }
                });
            }
            Composite paramsComposite = createParametersComposite(this);
            int index = 0;
            for (CalendarOperation operation : model.getOperations()) {
                addOperationSection(paramsComposite, operation, index);
                index++;
            }
        }

        private Composite createParametersComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(5, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            composite.setLayoutData(data);
            Composite strokeComposite = new Composite(composite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(5, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(Localization.getString("label.operations"));
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            SWTUtils.createLink(strokeComposite, Localization.getString("button.add"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addOperation(CalendarOperation.ADD);
                }
            });
            SWTUtils.createLink(strokeComposite, Localization.getString("button.set"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addOperation(CalendarOperation.SET);
                }
            });
            return composite;
        }

        private void addOperationSection(Composite parent, final CalendarOperation operation, final int index) {
            {
                final Button checkBusinessTimeButton = new Button(parent, SWT.CHECK);
                checkBusinessTimeButton.setText(Localization.getString("label.businessTime"));
                checkBusinessTimeButton.setEnabled(CalendarOperation.ADD.equals(operation.getType()));
                checkBusinessTimeButton.setSelection(operation.isBusinessTime());
                checkBusinessTimeButton.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    public void onSelection(SelectionEvent e) {
                        operation.setBusinessTime(checkBusinessTimeButton.getSelection());
                    }
                });
            }
            {
                final Combo combo = new Combo(parent, SWT.READ_ONLY);
                for (String fieldName : CalendarConfig.FIELD_NAMES) {
                    combo.add(fieldName);
                }
                combo.setText(operation.getFieldName());
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    public void onSelection(SelectionEvent e) {
                        operation.setFieldName(combo.getText());
                    }
                });
            }
            final Label label = new Label(parent, SWT.NONE);
            label.setText(operation.getType());
            {
                final Text text = new Text(parent, SWT.BORDER);
                text.setText(operation.getExpression());
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                text.addModifyListener(new LoggingModifyTextAdapter() {
                    @Override
                    public void onTextChanged(ModifyEvent e) {
                        operation.setExpression(text.getText());
                    }
                });
                List<String> variableNames = delegable.getVariableNames(false, Date.class.getName(), Long.class.getName());
                new InsertVariableTextMenuDetectListener(text, variableNames);
            }
            SWTUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.deleteOperation(index);
                }
            });
        }
    }
}

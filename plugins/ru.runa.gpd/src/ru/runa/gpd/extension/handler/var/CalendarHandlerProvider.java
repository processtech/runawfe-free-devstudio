package ru.runa.gpd.extension.handler.var;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class CalendarHandlerProvider extends XmlBasedConstructorProvider<CalendarConfig> {
    private static String[] dateFormats = new String[] { Date.class.getName() };
    private static String[] setFormats = new String[] { Date.class.getName(), Long.class.getName() };

    @Override
    protected CalendarConfig createDefault() {
        return new CalendarConfig();
    }

    @Override
    protected CalendarConfig fromXml(String xml) throws Exception {
        return CalendarConfig.fromXml(xml);
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
        CalendarConfig model = CalendarConfig.fromXml(delegable.getDelegationConfiguration());
        if (model != null) {
            if (!Strings.isNullOrEmpty(model.getBaseVariableName())) {
                result.add(model.getBaseVariableName());
            }
            if (!Strings.isNullOrEmpty(model.getOutVariableName())) {
                result.add(model.getOutVariableName());
            }
        }
        return result;
    }

    @Override
    protected boolean validateModel(Delegable delegable, CalendarConfig model, List<ValidationError> errors) {
        if (Strings.isNullOrEmpty(model.getOutVariableName())) {
            return false;
        }
        for (CalendarOperation operation : model.getOperations()) {
            if (operation.isBusinessTime() && !CalendarConfig.BUSINESS_FIELD_NAMES.contains(operation.getFieldName())) {
                errors.add(ValidationError.createLocalizedError((GraphElement) delegable, "delegable.calendar.businesstime.error"));
            }
        }
        return super.validateModel(delegable, model, errors);
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        CalendarConfig model = CalendarConfig.fromXml(delegable.getDelegationConfiguration());
        if (model != null) {
            if (Objects.equal(model.getBaseVariableName(), currentVariable.getName())) {
                model.setBaseVariableName(previewVariable.getName());
            }
            if (Objects.equal(model.getOutVariableName(), currentVariable.getName())) {
                model.setOutVariableName(previewVariable.getName());
            }
        }
        return model.toString();
    }

    private class ConstructorView extends ConstructorComposite {

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
                addRootSection();
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private GridData get2GridData() {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            return data;
        }

        private void addRootSection() {
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("property.duration.baseDate"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Duration.CURRENT_DATE_MESSAGE);
                for (String variableName : delegable.getVariableNames(false, dateFormats)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                if (model.getBaseVariableName() != null) {
                    combo.setText(model.getBaseVariableName());
                } else {
                    combo.setText(Duration.CURRENT_DATE_MESSAGE);
                }
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setBaseVariableName(combo.getText());
                        if (Duration.CURRENT_DATE_MESSAGE.equals(model.getBaseVariableName())) {
                            model.setBaseVariableName(null);
                        }
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("ParamBasedProvider.result"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(false, dateFormats)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                combo.setText(model.getOutVariableName());
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setOutVariableName(combo.getText());
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
                checkBusinessTimeButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
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
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
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
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        operation.setExpression(text.getText());
                    }
                });
                List<String> variableNames = delegable.getVariableNames(false, setFormats);
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

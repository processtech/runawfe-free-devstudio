package ru.runa.gpd.extension.handler.var;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class TargetProcessSetDateVariableHandlerProvider extends SetDateVariableHandlerProvider<TargetProcessCalendarConfig> {

    @Override
    protected String getTitle() {
        return Localization.getString("ru.runa.wfe.extension.handler.var.TargetProcessCalendarHandler");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, CalendarConfig config) {
        return new ConstructorView(parent, delegable, (TargetProcessCalendarConfig) config);
    }

    @Override
    protected TargetProcessCalendarConfig createDefault() {
        return new TargetProcessCalendarConfig();
    }

    @Override
    protected TargetProcessCalendarConfig fromXml(String xml) {
        return new TargetProcessCalendarConfig(xml);
    }

    @Override
    protected void fillUserVariableNames(List<String> result, TargetProcessCalendarConfig model) {
        super.fillUserVariableNames(result, model);
        if (!Strings.isNullOrEmpty(model.getProcessIdVariableName())) {
            result.add(model.getProcessIdVariableName());
        }
    }

    @Override
    protected boolean validateModel(Delegable delegable, CalendarConfig model, List<ValidationError> errors) {
        String processIdVariableName = ((TargetProcessCalendarConfig) model).getProcessIdVariableName();
        if (Strings.isNullOrEmpty(processIdVariableName)) {
            return false;
        }
        if (!delegable.getVariableNames(false, Long.class.getName()).contains(processIdVariableName)) {
            return false;
        }
        return super.validateModel(delegable, model, errors);
    }

    @Override
    protected boolean validateResultVariable(Delegable delegable, String resultVariableName) {
        if (Strings.isNullOrEmpty(resultVariableName)) {
            return false;
        }
        if (VariableUtils.isVariableNameWrapped(resultVariableName)) {
            if (!delegable.getVariableNames(false, String.class.getName()).contains(VariableUtils.unwrapVariableName(resultVariableName))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void applyConfigurationOnVariableRename(CalendarConfig model, Variable currentVariable, Variable previewVariable) {
        super.applyConfigurationOnVariableRename(model, currentVariable, previewVariable);
        if (Objects.equal(((TargetProcessCalendarConfig) model).getProcessIdVariableName(), currentVariable.getName())) {
            ((TargetProcessCalendarConfig) model).setProcessIdVariableName(previewVariable.getName());
        }
    }

    private class ConstructorView extends ru.runa.gpd.extension.handler.var.SetDateVariableHandlerProvider<TargetProcessCalendarConfig>.ConstructorView {

        public ConstructorView(Composite parent, Delegable delegable, TargetProcessCalendarConfig config) {
            super(parent, delegable, config);
        }

        private TargetProcessCalendarConfig getModel() {
            return (TargetProcessCalendarConfig) model;
        }

        @Override
        protected void addRootSection(boolean addResultVariableSection) {
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("Param.ProcessId"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.setLayoutData(get2GridData());
                for (String variableName : delegable.getVariableNames(false, Long.class.getName())) {
                    combo.add(variableName);
                }
                if (getModel().getProcessIdVariableName() != null) {
                    combo.setText(getModel().getProcessIdVariableName());
                }
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    public void onSelection(SelectionEvent e) {
                        getModel().setProcessIdVariableName(combo.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("ParamBasedProvider.result"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(get2GridData());
                if (model.getResultVariableName() != null) {
                    text.setText(model.getResultVariableName());
                }
                text.addModifyListener(new LoggingModifyTextAdapter() {
                    @Override
                    public void onTextChanged(ModifyEvent e) {
                        model.setResultVariableName(text.getText());
                    }

                });
                List<String> variableNames = delegable.getVariableNames(false, String.class.getName());
                new InsertVariableTextMenuDetectListener(text, variableNames);
            }
            super.addRootSection(false);
        }
    }

}

package ru.runa.gpd.office.word;

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
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.wfe.var.file.FileVariable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class MSWordTaskHandlerConfigurator extends XmlBasedConstructorProvider<MSWordConfig> {
    @Override
    protected MSWordConfig createDefault() {
        return new MSWordConfig();
    }

    @Override
    protected MSWordConfig fromXml(String xml) throws Exception {
        return MSWordConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, IDelegable iDelegable, MSWordConfig model) {
        return new ConstructorView(parent, iDelegable, model);
    }

    @Override
    protected int getSelectedTabIndex(IDelegable iDelegable, MSWordConfig model) {
        return iDelegable instanceof BotTask ? 1 : 0;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("MSWordConfig.title");
    }

    @Override
    public List<String> getUsedVariableNames(IDelegable iDelegable) {
        List<String> result = Lists.newArrayList();
        MSWordConfig model = MSWordConfig.fromXml(iDelegable.getDelegationConfiguration());
        if (model != null) {
            if (model.getResultVariableName() != null) {
                result.add(model.getResultVariableName());
            }
            for (MSWordVariableMapping mapping : model.getMappings()) {
                result.add(mapping.getVariableName());
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(IDelegable iDelegable, Variable currentVariable, Variable previewVariable) {
        MSWordConfig model = MSWordConfig.fromXml(iDelegable.getDelegationConfiguration());
        if (model != null) {
            if (Objects.equal(model.getResultVariableName(), currentVariable.getName())) {
                model.setResultVariableName(previewVariable.getName());
            }
            for (MSWordVariableMapping mapping : model.getMappings()) {
                if (Objects.equal(mapping.getVariableName(), currentVariable.getName())) {
                    mapping.setVariableName(previewVariable.getName());
                }
            }
        }
        return model.toString();
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, IDelegable iDelegable, MSWordConfig model) {
            super(parent, iDelegable, model);
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

        private GridData getGridData(int horizontalSpan) {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = horizontalSpan;
            return data;
        }

        private void addRootSection() {
            {
                final Button strict = new Button(this, SWT.CHECK);
                strict.setLayoutData(getGridData(3));
                strict.setText(Messages.getString("label.strict"));
                strict.setSelection(model.isStrictMode());
                strict.addSelectionListener(new LoggingSelectionAdapter() {

                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        model.setStrictMode(strict.getSelection());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.templatePath"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(getGridData(2));
                text.setText(model.getTemplatePath());
                text.addModifyListener(new LoggingModifyTextAdapter() {

                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        model.setTemplatePath(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultFileName"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(getGridData(2));
                text.setText(model.getResultFileName());
                text.addModifyListener(new LoggingModifyTextAdapter() {

                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        model.setResultFileName(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultVariableName"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (String variableName : iDelegable.getVariableNames(true, FileVariable.class.getName())) {
                    combo.add(variableName);
                }
                combo.setLayoutData(getGridData(2));
                combo.setText(model.getResultVariableName());
                combo.addSelectionListener(new LoggingSelectionAdapter() {

                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        model.setResultVariableName(combo.getText());
                    }
                });
            }
            Composite paramsComposite = createParametersComposite(this);
            int index = 0;
            for (MSWordVariableMapping mapping : model.getMappings()) {
                addParamSection(paramsComposite, mapping, index);
                index++;
            }
        }

        private Composite createParametersComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            composite.setLayoutData(data);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            Composite strokeComposite = SWTUtils.createStrokeComposite(composite, data, Messages.getString("MSWordConfig.label.mappings"), 4);
            SWTUtils.createLink(strokeComposite, Localization.getString("button.add"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addMapping();
                }
            });
            return composite;
        }

        private void addParamSection(Composite parent, final MSWordVariableMapping mapping, final int index) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : iDelegable.getVariableNames(true)) {
                combo.add(variableName);
            }
            combo.setText(mapping.getVariableName());
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String variableName = combo.getText();
                    mapping.setVariableName(variableName);
                }
            });
            final Text text = new Text(parent, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setText(mapping.getBookmarkName());
            text.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    mapping.setBookmarkName(text.getText());
                }
            });
            SWTUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.deleteMapping(index);
                }
            });
        }
    }
}

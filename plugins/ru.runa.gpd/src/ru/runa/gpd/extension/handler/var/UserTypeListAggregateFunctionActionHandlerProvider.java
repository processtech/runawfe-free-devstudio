package ru.runa.gpd.extension.handler.var;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinitionAware;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Lists;

public class UserTypeListAggregateFunctionActionHandlerProvider extends XmlBasedConstructorProvider<UserTypeListAggregateFunctionConfig> {

    @Override
    protected UserTypeListAggregateFunctionConfig createDefault() {
        return new UserTypeListAggregateFunctionConfig("", Lists.newArrayList(
            new UserTypeListAggregateFunctionConfig.Operation("", "SUM", "")));
    }

    @Override
    protected UserTypeListAggregateFunctionConfig fromXml(String xml) throws Exception {
        return UserTypeListAggregateFunctionConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, UserTypeListAggregateFunctionConfig model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("UserTypeListAggregateFunctionConfig.title");
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, Delegable delegable, UserTypeListAggregateFunctionConfig model) {
            super(parent, delegable, model);
            setLayout(new GridLayout(4, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                addListSection();
                addOperationsSection();
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (RuntimeException e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addListSection() {
            Label label = new Label(this, SWT.NONE);
            label.setText(Localization.getString("UserTypeListAggregateFunctionConfig.list"));
            final Combo combo = new Combo(this, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(false, List.class.getName())) {
                combo.add(variableName);
            }
            combo.setText(model.getListName());
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    model.setListName(combo.getText());
                }
            });
        }

        private void addOperationsSection() {
            Composite composite = new Composite(this, SWT.NONE);
            composite.setLayout(new GridLayout(4, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            composite.setLayoutData(data);

            createStrokeComposite(composite, Localization.getString("UserTypeListAggregateFunctionConfig.operations"), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addOperation(new UserTypeListAggregateFunctionConfig.Operation("", "SUM", ""));
                }
            });

            for (int i = 0; i < model.getOperations().size(); i++) {
                addOperationSection(composite, model.getOperations().get(i), i);
            }
        }

        private void createStrokeComposite(Composite parent, String label, LoggingHyperlinkAdapter hyperlinkAdapter) {
            Composite strokeComposite = new Composite(parent, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(hyperlinkAdapter != null ? 4 : 3, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(label);
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (hyperlinkAdapter != null) {
                SwtUtils.createLink(strokeComposite, Localization.getString("button.add"), hyperlinkAdapter);
            }
        }

        private void addOperationSection(Composite parent, UserTypeListAggregateFunctionConfig.Operation operation, int index) {
            // Attribute
            final Combo attributeCombo = new Combo(parent, SWT.READ_ONLY);
            String listName = model.getListName();
            if (!listName.isEmpty()) {
                Variable listVariable = VariableUtils.getVariableByName(((ProcessDefinitionAware) delegable).getProcessDefinition(), listName);
                if (listVariable != null && "ru.runa.wfe.var.format.ListFormat".equals(listVariable.getFormatClassName())) {
                    String[] componentTypes = listVariable.getFormatComponentClassNames();
                    if (componentTypes.length > 0) {
                        VariableUserType userType = ((ProcessDefinitionAware) delegable).getProcessDefinition().getTypeByName(componentTypes[0]);
                        if (userType != null) {
                            for (Variable attr : userType.getAttributes()) {
                                attributeCombo.add(attr.getName());
                            }
                        }
                    }
                }
            }
            attributeCombo.setText(operation.getAttribute());
            attributeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            attributeCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    model.updateOperation(index, new UserTypeListAggregateFunctionConfig.Operation(attributeCombo.getText(), operation.getFunction(), operation.getResult()));
                }
            });

            // Function
            final Combo functionCombo = new Combo(parent, SWT.READ_ONLY);
            functionCombo.add("SUM");
            functionCombo.add("AVERAGE");
            functionCombo.add("COUNT");
            functionCombo.add("MIN");
            functionCombo.add("MAX");
            functionCombo.setText(operation.getFunction());
            functionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            functionCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    model.updateOperation(index, new UserTypeListAggregateFunctionConfig.Operation(operation.getAttribute(), functionCombo.getText(), operation.getResult()));
                }
            });

            // Result
            final Combo resultCombo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(true)) {
                resultCombo.add(variableName);
            }
            resultCombo.setText(operation.getResult());
            resultCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            resultCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    model.updateOperation(index, new UserTypeListAggregateFunctionConfig.Operation(operation.getAttribute(), operation.getFunction(), resultCombo.getText()));
                }
            });

            // Delete
            SwtUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {
                
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.removeOperation(index);
                }
            });
        }
    }
}

package ru.runa.gpd.extension.handler.var;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.Duration;

public class DeadlineTransferHandlerProvider extends XmlBasedConstructorProvider<DeadlineTransferConfig> {
    private static String[] NumberFormats = new String[] { Integer.class.getName(), Long.class.getName() };
    private static String[] StringFormats = new String[] { String.class.getName() };
    private static String[] setFormats = new String[] { Date.class.getName(), Long.class.getName() };

    @Override
    protected String getTitle() {
        return Localization.getString("ru.runa.wfe.extension.handler.var.DeadlineTransferHandler");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, DeadlineTransferConfig config) {
        return new ConstructorView(parent, delegable, config);
    }

    @Override
    protected DeadlineTransferConfig createDefault() {
        return new DeadlineTransferConfig();
    }

    @Override
    protected DeadlineTransferConfig fromXml(String xml) throws Exception {
        return DeadlineTransferConfig.fromXml(xml);
    }

    private class ConstructorView extends ConstructorComposite {
        public ConstructorView(Composite parent, Delegable delegable, DeadlineTransferConfig config) {
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
                label.setText(Localization.getString("Param.ProcessId"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.setLayoutData(get2GridData());
                for (String variableName : delegable.getVariableNames(false, NumberFormats)) {
                    combo.add(variableName);
                }
                if (model.getProcessIdVariable() != null) {
                    combo.setText(model.getProcessIdVariable());
                } else {
                    combo.setText(Duration.CURRENT_DATE_MESSAGE);
                }
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setProcessIdVariable(combo.getText());
                        if (Duration.CURRENT_DATE_MESSAGE.equals(model.getProcessIdVariable())) {
                            model.setProcessIdVariable(null);
                        }
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("Param.Variable"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.setLayoutData(get2GridData());
                for (String variableName : delegable.getVariableNames(false, StringFormats)) {
                    combo.add(variableName);
                }
                combo.setText(model.getVariableName());
                combo.setEnabled(!model.getIsVariableInput());
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setVariableName(combo.getText());
                    }
                });
                Button checkBox = new Button(this, SWT.CHECK);
                checkBox.setText(Localization.getString("ru.runa.wfe.extension.handler.var.DeadlineTransferHandler.isInput"));
                checkBox.setSelection(model.getIsVariableInput());
                final Text textField = new Text(this, SWT.BORDER);
                textField.setLayoutData(get2GridData());
                if (model.getIsVariableInput()) {
                    textField.setText(model.getVariableName());
                }
                textField.setEnabled(model.getIsVariableInput());
                textField.addKeyListener(new KeyListener() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        model.setVariableName(textField.getText());
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                    }
                });
                checkBox.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Button button = (Button) e.getSource();
                        if (button.getSelection()) {
                            combo.setEnabled(false);
                            textField.setEnabled(true);
                            model.setIsVariableInput(true);
                            model.setVariableName(textField.getText());
                        } else {
                            textField.setEnabled(false);
                            combo.setEnabled(true);
                            model.setIsVariableInput(false);
                            model.setVariableName(combo.getText());
                        }

                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
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

package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormGpdVariable;
import ru.runa.gpd.quick.tag.FreemarkerConfigurationGpdWrap;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;

public class QuickFormVariableWizardPage extends WizardPage {
    private ComboViewer tagType;
    private Combo variableCombo;
    private final QuickFormGpdVariable variableDef;
    private String paramValue;

    private final FormNode formNode;

    protected QuickFormVariableWizardPage(FormNode formNode, QuickFormGpdVariable variableDef) {
        super(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setTitle(Messages.getString("TemplatedFormVariableWizardPage.page.title"));
        setDescription(Messages.getString("TemplatedFormVariableWizardPage.page.description"));
        this.formNode = formNode;
        this.variableDef = new QuickFormGpdVariable();
        if (variableDef != null) {
            this.variableDef.setTagName(variableDef.getTagName());
            this.variableDef.setName(variableDef.getName());
            this.variableDef.setScriptingName(variableDef.getScriptingName());
            this.variableDef.setDescription(variableDef.getDescription());
            this.variableDef.setFormatLabel(variableDef.getFormatLabel());
            this.variableDef.setParams(variableDef.getParams());
        }
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        rebuildView(parent);
    }

    private void rebuildView(Composite parent) {
        for (Control control : parent.getChildren()) {
            control.dispose();
        }

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createTagTypeField(composite);
        createVariablesField(composite);
        populateValues();
        createParamField(composite);
        verifyContentsValid();
        setControl(composite);
        Dialog.applyDialogFont(composite);
        parent.layout(true, true);
        if (variableDef == null) {
            setPageComplete(false);
        }
    }

    private void verifyContentsValid() {
        if (tagType.getCombo().getText().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_tag"));
            setPageComplete(false);
        } else if (variableCombo.getText().length() == 0) {
            setErrorMessage(Messages.getString("TemplatedFormVariableWizardPage.error.no_variable"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    private void createTagTypeField(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.tag"));
        List<SelectItem> types = new ArrayList<SelectItem>();
        FreemarkerConfigurationGpdWrap freemarkerConfiguration = FreemarkerConfigurationGpdWrap.getInstance();

        for (String value : freemarkerConfiguration.getTagsName()) {
            if (ComponentTypeRegistry.has(value)) {
                ComponentType tag = ComponentTypeRegistry.getNotNull(value);
                types.add(new SelectItem(tag.getLabel(), value));
                continue;
            }
        }

        tagType = new ComboViewer(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);

        tagType.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagType.setContentProvider(ArrayContentProvider.getInstance());
        tagType.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof SelectItem) {
                    SelectItem current = (SelectItem) element;

                    return current.getLabel();
                }
                return "";
            }
        });
        tagType.setInput(types.toArray(new SelectItem[types.size()]));
        tagType.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent e) {
                IStructuredSelection selection = (IStructuredSelection) e.getSelection();
                SelectItem selectItem = (SelectItem) selection.getFirstElement();

                variableDef.setTagName(selectItem.getValue().toString());

                rebuildView(parent.getParent());
                verifyContentsValid();
            }
        });
    }

    private void createVariablesField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("TemplatedFormVariableWizardPage.page.var") + " *");
        variableCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        variableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        List<String> names = formNode.getVariableNames(true);

        variableCombo.setItems(names.toArray(new String[names.size()]));
        variableCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {

                verifyContentsValid();

            }
        });
    }

    private void createParamField(Composite parent) {
        Map<String, ComponentType> componentTypes = ComponentTypeRegistry.getAll();
        if (componentTypes != null) {
            for (ComponentType componentType : componentTypes.values()) {
                if (componentType.getLabel().equals(tagType.getCombo().getText())) {
                    if (componentType.getParameters().size() < 2) {
                        paramValue = null;
                    }
                    for (int i = 1; i < componentType.getParameters().size(); i++) {
                        ComponentParameter componentParameter = componentType.getParameters().get(i);
                        Label label = new Label(parent, SWT.NONE);
                        label.setText(componentParameter.getLabel());
                        // TODO in createEditor variables populated from
                        // FormEditor instance
                        componentParameter.getType().createEditor(parent, componentParameter, paramValue, new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                paramValue = evt.getNewValue().toString();
                                verifyContentsValid();

                            }
                        });
                    }
                    break;
                }
            }
        }
    }

    private void populateValues() {
        if (variableDef != null && variableDef.getTagName() != null) {
            SelectItem[] selectItems = (SelectItem[]) tagType.getInput();
            for (SelectItem selectItem : selectItems) {
                if (variableDef.getTagName().equals(selectItem.getValue())) {
                    tagType.getCombo().setText(selectItem.getLabel());
                    break;
                }
            }
        }
        if (variableDef != null && variableDef.getName() != null) {
            variableCombo.setText(variableDef.getName());
        }
        if (variableDef != null && variableDef.getParams() != null && variableDef.getParams().length > 0) {
            paramValue = variableDef.getParams()[0];
        }
    }

    public String getTagType() {
        SelectItem[] selectItems = (SelectItem[]) tagType.getInput();
        for (SelectItem selectItem : selectItems) {
            if (tagType.getCombo().getText().equals(selectItem.getLabel())) {
                return selectItem.getValue().toString();
            }
        }

        return "";
    }

    public Variable getVariable() {
        if (!Strings.isNullOrEmpty(variableCombo.getText())) {
            return VariableUtils.getVariableByName(formNode, variableCombo.getText());
        }
        return null;
    }

    public String getParamValue() {
        return paramValue;
    }

    public static class SelectItem {
        private String label;
        private Object value;

        public SelectItem(String label, Object value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}

package ru.runa.gpd.quick.formeditor.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.quick.Activator;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;
import ru.runa.gpd.quick.formeditor.QuickFormType;
import ru.runa.gpd.quick.formeditor.settings.PreferencePage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableUtils;

public class QuickFormVariablesToDisplayWizardPage extends WizardPage {
    private final FormNode formNode;
    private Button checkAllCheckbox;
    private final List<QuickFormComponent> selectedVariables = new ArrayList<>();
    private final List<QuickFormComponent> initialVariables;
    private ScrolledComposite scrolledComposite;
    private Composite checkboxesArea;
    private final ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
        @Override
        public void expansionStateChanged(ExpansionEvent e) {
            refreshGUI();
        }
    };
    private final Listener resizeListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            refreshGUI();
        }
    };

    protected QuickFormVariablesToDisplayWizardPage(FormNode formNode, List<QuickFormComponent> quickFormVariableDefs) {
        super(Messages.getString("QuickFormVariablesToDisplayWizardPage.page.title"));
        setTitle(Messages.getString("QuickFormVariablesToDisplayWizardPage.page.title"));
        setDescription(Messages.getString("QuickFormVariablesToDisplayWizardPage.page.description"));
        this.formNode = formNode;
        this.initialVariables = quickFormVariableDefs != null ? quickFormVariableDefs : new ArrayList<>();
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        createView(parent);
    }

    private void createView(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createCheckAllVariables(composite);
        createVariableCheckboxes(composite);
        refreshCheckAllCheckbox();

        setControl(composite);
        Dialog.applyDialogFont(composite);
        validateSections(); // Изначальная валидация. Неправильные значения параметров могут быть, например, в загруженном процессе
    }

    private void createCheckAllVariables(final Composite parent) {
        checkAllCheckbox = new Button(parent, SWT.CHECK);
        checkAllCheckbox.setText(Messages.getString("QuickFormVariablesToDisplayWizardPage.selectall.label"));
        checkAllCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                boolean checkAllCheckboxSelected = checkAllCheckbox.getSelection();
                for (Control i : checkboxesArea.getChildren()) {
                    QuickFormComponentEditingSection section = (QuickFormComponentEditingSection) i;
                    if (section.getVariableCheckboxSelection() != checkAllCheckboxSelected) {
                        section.setVariableCheckboxSelection(checkAllCheckboxSelected);
                        section.notifyVariableCheckboxListeners(SWT.Selection, new Event());
                    }
                }
                validateSections();
            }
        });
    }

    private void createVariableCheckboxes(final Composite parent) {
        scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        checkboxesArea = new Composite(scrolledComposite, SWT.NONE);

        checkboxesArea.setLayout(new GridLayout(1, true));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        checkboxesArea.setLayoutData(gridData);

        for (String name : formNode.getVariableNames(true)) {
            QuickFormComponent paramVariable = null;
            for (QuickFormComponent variable : initialVariables) {
                if (name.equals(variable.getName())) {
                    paramVariable = variable.copy(); // копируется, чтобы его изменения
                    // в диалоге потом не оставлялись, когда нажимается "Отмена"
                    break;
                }
            }
            boolean paramVariableIsNull = paramVariable == null;
            if (paramVariableIsNull) {
                paramVariable = new QuickFormComponent(); // может вынести это все в отдельный метод
                Variable variable = VariableUtils.getVariableByName(formNode, name);
                paramVariable.setTagName(QuickFormType.READ_TAG);
                paramVariable.fillFromVariable(variable);
                List<Object> params = new ArrayList<>(2);
                params.add(variable.getName());
                params.add(Activator.getDefault().getPreferenceStore().getString(PreferencePage.P_FORM_DEFAULT_DISPLAY_FORMAT));
                paramVariable.setParams(params);
            } else {
                selectedVariables.add(paramVariable);
            }
            QuickFormComponentEditingSection section = new QuickFormComponentEditingSection(checkboxesArea, paramVariable,
                    formNode.getProcessDefinition());
            section.setVariableCheckboxSelection(!paramVariableIsNull);
            final QuickFormComponent paramVariableConstant = paramVariable;
            section.addVariableCheckboxSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    if (selectedVariables.contains(paramVariableConstant)) {
                        selectedVariables.remove(paramVariableConstant);
                    } else {
                        selectedVariables.add(paramVariableConstant);
                    }
                    refreshCheckAllCheckbox();
                    validateSections();
                }
            });
            section.addExpansionListener(this.expansionAdapter);
            section.addResizeListener(this.resizeListener);
            section.addValidationListener(new Listener() {

                @Override
                public void handleEvent(Event event) {
                    if (section.getVariableCheckboxSelection()) {
                        validateSections();
                    }
                }
            });
        }
        scrolledComposite.setContent(checkboxesArea);
        refreshGUI();
    }

    private void refreshCheckAllCheckbox() {
        checkAllCheckbox.setSelection(selectedVariables.size() == checkboxesArea.getChildren().length);
    }

    private void refreshGUI() {
        scrolledComposite.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public List<QuickFormComponent> getSelectedVariables() {
        return selectedVariables;
    }

    private void validateSections() {
        String validationError = getFirstValidationError();
        setPageComplete(validationError == null ? true : false);
        setErrorMessage(validationError);
    }

    private String getFirstValidationError() {
        for (Control i : checkboxesArea.getChildren()) {
            QuickFormComponentEditingSection section = (QuickFormComponentEditingSection) i;
            QuickFormComponent quickFormComponent = section.getQuickFormComponent();
            if (selectedVariables.contains(quickFormComponent)) {
                String validationError = section.validationError();
                if (validationError != null) {
                    return Messages.getString("QuickFormVariablesToDisplayWizardPage.error.validationErrorForVariable") + " '"
                            + quickFormComponent.getName() + "': " + validationError;
                }
            }
        }
        return null;
    }
}
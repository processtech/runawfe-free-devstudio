package ru.runa.gpd.ui.wizard;

import com.google.common.base.Strings;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.DynaContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

public class VariableDefaultValuePage extends DynaContentWizardPage {
    private String defaultValue;
    private Button useDefaultValueButton;

    public VariableDefaultValuePage(Variable variable) {
        if (variable != null) {
            this.defaultValue = variable.getDefaultValue();
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        dynaComposite = composite;
    }

    @Override
    protected void createDynaContent() {
        Button dontUseDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        dontUseDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.dontUse"));
        useDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        useDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.use"));
        String formatClassName = ((VariableWizard) getWizard()).getFormatPage().getType().getJavaClassName();
        boolean isFileMode = EmbeddedFileUtils.isFileVariableClassName(formatClassName);
        if (isFileMode) {
            if (!EmbeddedFileUtils.isProcessFile(defaultValue)) {
                defaultValue = null;
            }

            final DefaultFileVariableValueComposite defaultFileVariableValueComposite = new DefaultFileVariableValueComposite(this, defaultValue,
                    consumedDefaultValue -> defaultValue = consumedDefaultValue);
            useDefaultValueButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    boolean useDefaultValue = useDefaultValueButton.getSelection();
                    defaultFileVariableValueComposite.setEnabled(useDefaultValue);
                    if (!useDefaultValue && EmbeddedFileUtils.isProcessFile(defaultValue)) {
                        IFile file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue));
                        if (file != null) {
                            EmbeddedFileUtils.deleteProcessFile(file);
                            defaultValue = "";
                        }
                    }
                }
            });
        } else {
            final Text text = new Text(dynaComposite, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (defaultValue != null) {
                text.setText(defaultValue);
            }
            text.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    defaultValue = text.getText();
                    verifyContentIsValid();
                }
            });
            useDefaultValueButton.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    boolean useDefaultValue = useDefaultValueButton.getSelection();
                    text.setEditable(useDefaultValue);
                    if (!useDefaultValue) {
                        defaultValue = "";
                    }
                    verifyContentIsValid();
                }
            });
        }
        useDefaultValueButton.setSelection(!Strings.isNullOrEmpty(defaultValue));
        dontUseDefaultValueButton.setSelection(!useDefaultValueButton.getSelection());
    }

    private void verifyDefaultValue() throws Exception {
        VariableFormatPage formatPage = (VariableFormatPage) getWizard().getPage(VariableFormatPage.class.getSimpleName());
        if (formatPage.getUserType() != null || formatPage.getComponentClassNames().length > 0 /* List|Map */) {
            // TODO validate UserType attributes
            Object value = JSONValue.parse(defaultValue.replaceAll("&quot;", "\""));
            if ((formatPage.getUserType() != null || formatPage.getComponentClassNames().length == 2 /* Map */)) {
                if (value == null || !(value instanceof JSONObject)) {
                    throw new Exception(Localization.getString("VariableDefaultValuePage.error.expected.json_object"));
                }
            } else if (formatPage.getComponentClassNames().length == 1 /* List */ && (value == null || !(value instanceof JSONArray))) {
                throw new Exception(Localization.getString("VariableDefaultValuePage.error.expected.json_array"));
            }
        } else {
            String className = formatPage.getType().getJavaClassName();
            if (Group.class.getName().equals(className) || Actor.class.getName().equals(className) || Executor.class.getName().equals(className)) {
                // TODO validate using connection?
            } else if (EmbeddedFileUtils.isFileVariableClassName(className)) {
                if (EmbeddedFileUtils.isProcessFile(defaultValue)) {
                    IFile file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue));
                    if (null == file || !file.exists()) {
                        throw new Exception("File '" + defaultValue + "' does not exist!");
                    }
                } else {
                    throw new Exception("incorrect value");
                }
            } else {
                TypeConversionUtil.convertTo(ClassLoaderUtil.loadClass(className), defaultValue);
            }
        }
    }

    public boolean isValidContent() {
        try {
            if (defaultValue != null && !defaultValue.isEmpty()) {
                verifyDefaultValue();
                setErrorMessage(null);
            }
            return true;
        } catch (Exception e) {
            setErrorMessage(Localization.getString("VariableDefaultValuePage.error.conversion") + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void verifyContentIsValid() {
        try {
            if (useDefaultValueButton != null && useDefaultValueButton.getSelection() && defaultValue != null) {
                verifyDefaultValue();
            }
            setErrorMessage(null);
        } catch (Exception e) {
            setErrorMessage(Localization.getString("VariableDefaultValuePage.error.conversion") + ": " + e.getMessage());
        }
    }

    Composite getDynaComposite() {
        return dynaComposite;
    }

}

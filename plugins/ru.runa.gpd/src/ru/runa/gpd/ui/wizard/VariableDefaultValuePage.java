package ru.runa.gpd.ui.wizard;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONValue;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.DynaContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.file.FileVariable;
import com.google.common.base.Strings;

public class VariableDefaultValuePage extends DynaContentWizardPage {
    private String defaultValue;
    private Button useDefaultValueButton; 
    private IFile file = null;
    

    public VariableDefaultValuePage(Variable variable) {
        if (variable != null) {
            this.defaultValue = variable.getDefaultValue();
        }
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

        // Check FileFormat mode
        boolean isFileMode = isFileChooseMode();
       
    	// Create file variable with defaultValue checking (in file mode)
        file = null;
    	if (isFileMode) {
    		if (isEmbeddedFileWithProtocol())
    			file = EmbeddedFileUtils.getProcessFile(defaultValue);
    		else 
    			defaultValue = "";
        } 
    	
    	// Check "Use default" radio buttons group
        Button dontUseDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        dontUseDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.dontUse"));
        useDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        useDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.use"));
                
        // Create "File choose" button if needed
        Button _fileChooseButton = null;
        if (isFileMode) {
        	_fileChooseButton = new Button(dynaComposite, SWT.NONE);
        	_fileChooseButton.setText(Localization.getString("button.choose"));
        }
        final Button fileChooseButton = _fileChooseButton;
        
        // Default value text box
        final Text text = new Text(dynaComposite, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (defaultValue != null) {
        	text.setText(defaultValue);
        }
        text.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
            	if (!isFileMode)
            		defaultValue = text.getText();
                verifyContentIsValid();
            }
        });
        
        // onSelect event of "Use default" radio buttons group
        useDefaultValueButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                if (fileChooseButton != null) {
                	fileChooseButton.setEnabled(useDefaultValueButton.getSelection());
                	text.setEditable(false);
                } else
                	text.setEditable(useDefaultValueButton.getSelection());
                
                if (!useDefaultValueButton.getSelection()) {
                    text.setText(defaultValue = "");
                    if (null != file && file.exists())
        				EmbeddedFileUtils.deleteProcessFile(file);
                }
                verifyContentIsValid();
            }
        });
        
        // Set "Use default" radio buttons group
        if (Strings.isNullOrEmpty(defaultValue)) {
            dontUseDefaultValueButton.setSelection(true);
        } else {
            useDefaultValueButton.setSelection(true);
        }
        
        // onClick of "File choose" button if needed 
        if (fileChooseButton != null) {
        	text.setEditable(false);
        	fileChooseButton.setEnabled(useDefaultValueButton.getSelection());
        	
        	fileChooseButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                	FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                    String filePath = dialog.open();
                    if (filePath == null)
                        return;
                    
            		File f = new File(filePath);
            		if (f.exists())
            		{
            			if (IOUtils.looksLikeFormFile(filePath)) {
            				setErrorMessage(Localization.getString("VariableDefaultValuePage.error.ReservedFileName") + ": " + filePath);
            				return;
            			}   
            			setErrorMessage(null);
            			
            			if (null != file && file.exists())
            				EmbeddedFileUtils.deleteProcessFile(file);
            			
            			file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFilePath(f.getName()));
            			IOUtils.copyFile(filePath, file);
            			
            			text.setText(defaultValue = EmbeddedFileUtils.getProcessFilePath(file.getName()));            			
            		}
                }
            });
        } else
        	text.setEditable(useDefaultValueButton.getSelection());
    }

    @Override
    protected void verifyContentIsValid() {
        try {
            if (useDefaultValueButton != null && useDefaultValueButton.getSelection() && defaultValue != null) {
                VariableFormatPage formatPage = (VariableFormatPage) getWizard().getPage(VariableFormatPage.class.getSimpleName());
                if (formatPage.getUserType() != null || formatPage.getComponentClassNames().length > 0 /* List|Map */) {
                    // TODO validate UserType attributes
                    if (JSONValue.parse(defaultValue.replaceAll("&quot;", "\"")) == null) {
                        throw new Exception(Localization.getString("VariableDefaultValuePage.error.expected.json"));
                    }
                } else {
                    String className = formatPage.getType().getJavaClassName();
                    if (Group.class.getName().equals(className) || Actor.class.getName().equals(className)
                            || Executor.class.getName().equals(className)) {
                        // TODO validate using connection?
                    } else if (FileVariable.class.getName().equals(className)) {
                    	if (isUseDefaultValue()) {
	                    	if (isEmbeddedFileWithProtocol()) {
	                    		file = EmbeddedFileUtils.getProcessFile(defaultValue);
	                    		if (null == file || !file.exists())
	                    			throw new Exception("IFile '" + defaultValue + "' is not exists!");
	                    	} else
	                    		throw new Exception("The defaultValue '" + defaultValue + "' is not starts with protocol!");
                    	}
                    } else {
                        TypeConversionUtil.convertTo(ClassLoaderUtil.loadClass(className), defaultValue);
                    }
                }
            }
            setErrorMessage(null);
        } catch (Exception e) {
            setErrorMessage(Localization.getString("VariableDefaultValuePage.error.conversion") + ": " + e.getMessage());
        }
    }
    
    private boolean isUseDefaultValue() {
    	return !Strings.isNullOrEmpty(defaultValue);
    }
    
    private boolean isEmbeddedFileWithProtocol() {
    	return isUseDefaultValue() && EmbeddedFileUtils.isProcessFile(defaultValue);
    }
    
    public String getDefaultValue() {
    	return defaultValue;
    }
    
    private boolean isFileChooseMode() {
    	return FileVariable.class.getName().equals(
    			((VariableFormatPage) getWizard().getPage(VariableFormatPage.class.getSimpleName()))
        		.getType()
        		.getJavaClassName());
    }
}

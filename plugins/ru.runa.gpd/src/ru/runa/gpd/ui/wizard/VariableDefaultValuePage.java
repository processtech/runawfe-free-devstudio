package ru.runa.gpd.ui.wizard;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
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

        boolean isFileMode = isFileChooseMode();
       
        file = null;
    	if (isFileMode) {
    		if (isEmbeddedFileWithProtocol())
    			file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue));
    		else 
    			defaultValue = "";
        } 
    	
        Button dontUseDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        dontUseDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.dontUse"));
        useDefaultValueButton = new Button(dynaComposite, SWT.RADIO);
        useDefaultValueButton.setText(Localization.getString("VariableDefaultValuePage.use"));
                
        Button button = null;
        Label label = null;
        Text text = null;
        
        if (isFileMode) {
        	FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);        			
        	fillLayout.spacing = 15;

        	Composite сomposite = new Composite( dynaComposite, SWT.NONE );
        	сomposite.setLayout(fillLayout);        	
        	button = new Button(сomposite, SWT.NONE);
        	button.setText(Localization.getString("button.choose"));
        	label = new Label(сomposite, SWT.NO_FOCUS);
        	
        	if (defaultValue != null && file != null)
        		label.setText(EmbeddedFileUtils.getProcessFileName(defaultValue));
        	
        } else {        
        	text = new Text(dynaComposite, SWT.BORDER);
        	text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            if (defaultValue != null)
            	text.setText(defaultValue);
        }
        
        final Button fileButton = button;
        final Label fileLabel = label;
        final Text variableText = text;
        
        if (!isFileMode) {
        	variableText.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                	defaultValue = variableText.getText();
                    verifyContentIsValid();
                }
            });
        }
        
        
        useDefaultValueButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            	
            	boolean useDefaultValue = useDefaultValueButton.getSelection();
                 
                if (null != variableText)
                	variableText.setEditable(useDefaultValue);
                if (null != fileButton)
                	fileButton.setEnabled(useDefaultValue);
                
                if (!useDefaultValue) {
                	defaultValue = "";
                	if (null != variableText)
                		variableText.setText("");
                	if (null != fileLabel)
                		fileLabel.setText("");
                    
                    if (null != file && file.exists())
        				EmbeddedFileUtils.deleteProcessFile(file);
                }
                verifyContentIsValid();
            }
        });
        
        if (isUseDefaultValue()) 
        	useDefaultValueButton.setSelection(true);        
        else
        	dontUseDefaultValueButton.setSelection(true);
            
        
        if (isFileMode) {
        	fileButton.setEnabled(useDefaultValueButton.getSelection());
        	
        	fileButton.addSelectionListener(new LoggingSelectionAdapter() {
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

            			IFile newFile = EmbeddedFileUtils.getProcessFile(f.getName());
            			
            			if (newFile.exists() && newFile.getName().compareToIgnoreCase(file.getName()) != 0) {
            				String s = Localization.getString("VariableDefaultValuePage.error.EmbeddedFileNameAlreadyInUse") + ": " + newFile.getName();
            				MessageBox messageBox = new MessageBox(dynaComposite.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        			        messageBox.setMessage(s);
        			        messageBox.setText(Localization.getString("confirm.continue"));
        			        int response = messageBox.open();
        			        if (response == SWT.NO)
        			        	return;
            			}
            			
            			
            			if (null != file && file.exists())
							EmbeddedFileUtils.deleteProcessFile(file);

            			file = newFile;
            			
            			IOUtils.copyFile(filePath, file);
            			
            			defaultValue = EmbeddedFileUtils.getProcessFilePath(file.getName());
            			fileLabel.setText(EmbeddedFileUtils.getProcessFileName(defaultValue));
            			fileLabel.getParent().pack();
            		}
                }
            });
        	
        } else 
        	variableText.setEditable(useDefaultValueButton.getSelection());
        
        	
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
	                    		file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue));
	                    		if (null == file || !file.exists())
	                    			throw new Exception("IFile '" + defaultValue + "' does not exists!");
	                    	} else
	                    		throw new Exception("The defaultValue '" + defaultValue + "' does not starts with protocol!");
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

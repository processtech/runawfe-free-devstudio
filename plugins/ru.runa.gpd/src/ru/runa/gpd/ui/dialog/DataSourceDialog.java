package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.DataSourceType;
import ru.runa.wfe.datasource.JdbcDataSourceType;

public class DataSourceDialog extends Dialog implements DataSourceStuff {
    
    private final static char colon = ':';

    private final IFile ds;

    private Text txtName;
    private Combo cbType;
    private Text txtJndiName;
    private Text txtFilePath;
    private Text txtFileName;
    private Combo cbDbType;
    private Text txtDbUrl;
    private Text txtDbName;
    private Text txtUserName;
    
    private StackLayout stackLayout = new StackLayout();
    private Map<DataSourceType, Control> paneCache = new HashMap<>();
    private String name;
    private String xml;

    public DataSourceDialog(IFile ds) {
        super(Display.getDefault().getActiveShell());
        this.ds = ds;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(1, false));
        Composite composite = new Composite(area, SWT.FILL);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.name") + ":");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 500;
        txtName = new Text(composite, SWT.BORDER);
        txtName.setLayoutData(gd);
        txtName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateButtons();
            }
        });
        
        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.type") + ":");
        cbType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        cbType.setLayoutData(gd);
        for (DataSourceType dsType : DataSourceType.values()) {
            cbType.add(dsType.name());
        }
        
        final Composite panes = new Composite(composite, SWT.FILL);
        GridData gdPanes = new GridData(GridData.FILL_BOTH);
        gdPanes.horizontalSpan = 2;
        panes.setLayoutData(gdPanes);
        cbType.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                stackLayout.topControl = paneCache.get(DataSourceType.valueOf(cbType.getText()));
                panes.layout();
                updateButtons();
            }
        });
        panes.setLayout(stackLayout);
        createExcelPane(panes);
        createJdbcPane(panes);
        createJndiPane(panes);

        initWidgets();

        return area;
    }
    
    private void createExcelPane(Composite parent) {
        Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(GridData.FILL_BOTH));
        pane.setLayout(createPaneLayout());
        Label label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.filePath") + colon);
        txtFilePath = new Text(pane, SWT.BORDER);
        txtFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.fileName") + colon);
        txtFileName = new Text(pane, SWT.BORDER);
        txtFileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        paneCache.put(DataSourceType.Excel, pane);
    }
    
    private void createJdbcPane(Composite parent) {
        Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(GridData.FILL_BOTH));
        pane.setLayout(createPaneLayout());
        Label label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.dbType") + colon);
        cbDbType = new Combo(pane, SWT.BORDER | SWT.READ_ONLY);
        cbDbType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (JdbcDataSourceType dsType : JdbcDataSourceType.values()) {
            cbDbType.add(dsType.name());
        }
        cbDbType.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                txtDbUrl.setText(JdbcDataSourceType.valueOf(cbDbType.getText()).urlSample());
                String jndiName = txtJndiName.getText();
                txtJndiName.setText(Strings.isNullOrEmpty(jndiName) ? JNDI_NAME_SAMPLE : jndiName);
                updateButtons();
            }
        });
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.dbUrl") + colon);
        txtDbUrl = new Text(pane, SWT.BORDER);
        txtDbUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.dbName") + colon);
        txtDbName = new Text(pane, SWT.BORDER);
        txtDbName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.userName") + colon);
        txtUserName = new Text(pane, SWT.BORDER);
        txtUserName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        paneCache.put(DataSourceType.JDBC, pane);
    }
    
    private void createJndiPane(Composite parent) {
        Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(GridData.FILL_BOTH));
        pane.setLayout(createPaneLayout());
        Label label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.jndiName") + colon);
        txtJndiName = new Text(pane, SWT.BORDER);
        txtJndiName.setText(JNDI_NAME_SAMPLE);
        txtJndiName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        paneCache.put(DataSourceType.JNDI, pane);
    }
    
    private GridLayout createPaneLayout() {
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        return gl;
    }
    
    private void initWidgets() {
        if (ds != null) {
            try (InputStream is = ds.getContents()) {
                Document document = XmlUtil.parseWithoutValidation(is);
                Element root = document.getRootElement();
                txtName.setText(root.attribute(ATTR_NAME).getValue());
                String type = root.attribute(ATTR_TYPE).getValue();
                cbType.setText(type);
                switch (DataSourceType.valueOf(type)) {
                case Excel:
                    txtFilePath.setText(root.element(ELEMENT_FILE_PATH).getText());
                    txtFileName.setText(root.element(ELEMENT_FILE_NAME).getText());
                    break;
                case JDBC:
                    cbDbType.setText(root.element(ELEMENT_DB_TYPE).getText());
                    txtDbUrl.setText(root.element(ELEMENT_DB_URL).getText());
                    txtDbName.setText(root.element(ELEMENT_DB_NAME).getText());
                    txtUserName.setText(root.element(ELEMENT_USER_NAME).getText());
                    break;
                default: // JNDI
                    String jndiName = root.element(ELEMENT_JNDI_NAME).getText();
                    txtJndiName.setText(Strings.isNullOrEmpty(jndiName) ? JNDI_NAME_SAMPLE : jndiName);
                }
            } catch (IOException | CoreException e) {
                throw new InternalApplicationException(e);
            }
        }
    }
    
    private String asXmlString() {
        Document document = DocumentHelper.createDocument();
        Element dataSource = document.addElement(ELEMENT_DATA_SOURCE);
        dataSource.addAttribute(ATTR_NAME, txtName.getText());
        dataSource.addAttribute(ATTR_TYPE, cbType.getText());
        switch (DataSourceType.valueOf(cbType.getText())) {
        case Excel:
            dataSource.addElement(ELEMENT_FILE_PATH).addText(txtFilePath.getText());
            dataSource.addElement(ELEMENT_FILE_NAME).addText(txtFileName.getText());
            break;
        case JDBC:
            dataSource.addElement(ELEMENT_DB_TYPE).addText(cbDbType.getText());
            dataSource.addElement(ELEMENT_DB_URL).addText(txtDbUrl.getText());
            dataSource.addElement(ELEMENT_DB_NAME).addText(txtDbName.getText());
            dataSource.addElement(ELEMENT_USER_NAME).addText(txtUserName.getText());
            break;
        default: // JNDI
            dataSource.addElement(ELEMENT_JNDI_NAME).addText(txtJndiName.getText());
        }
        return XmlUtil.toString(document);
    }

    public String getName() {
        return name;
    }

    public String getXml() {
        return xml;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID); 
        okButton.setEnabled(ds != null);
        okButton.setText(Localization.getString(ds == null ? "datasource.dialog.button.label.add" : "datasource.dialog.button.label.save"));
    }

    private void updateButtons() {
        Button btnOk = getButton(IDialogConstants.OK_ID);
        if (btnOk != null) {
            String name = txtName.getText().trim();
            boolean canBeSaved = name.length() > 0
                    && (ds != null && ds.getName().equals(name + DATA_SOURCE_FILE_SUFFIX)
                            || !DataSourceUtils.getDataSourcesProject().getFile(name + DATA_SOURCE_FILE_SUFFIX).exists())
                    && cbType.getText().length() > 0
                    && (!DataSourceType.valueOf(cbType.getText()).equals(DataSourceType.JDBC) || cbDbType.getText().length() > 0);
            btnOk.setEnabled(canBeSaved);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImage(SharedImages.getImage("icons/data_sources.png"));
        newShell.setText(Localization.getString(ds == null ? "datasource.dialog.title.label.add" : "datasource.dialog.title.label.edit"));
    }

    @Override
    protected void okPressed() {
        name = txtName.getText();
        xml = asXmlString();
        super.okPressed();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

}

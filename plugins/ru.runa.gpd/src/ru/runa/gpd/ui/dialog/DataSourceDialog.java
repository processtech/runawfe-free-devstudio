package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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
    private Combo cbDbType;
    private Text txtDbUrl;
    private Text txtDbHost;
    private Text txtDbHostPort;
    private Text txtDbName;
    private Text txtParamUserName;
    private Text txtParamPassword;
    private Text txtUrlUserName;
    private Text txtUrlPassword;
    private Button rbParams;
    private Button rbUrl;

    private StackLayout stackLayout = new StackLayout();
    private StackLayout stackLayoutDetail = new StackLayout();
    private Map<DataSourceType, Control> paneCache = new HashMap<>();
    private Control[] paneCacheDetail = new Control[2];
    private String name;
    private String xml;

    private int subpaneState;
    private Composite subPanes;

    public DataSourceDialog(IFile ds) {
        super(Display.getDefault().getActiveShell());
        this.ds = ds;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        subpaneState = SUBPANE_PARAM_STATE; // Set initial subpane state
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
        paneCache.put(DataSourceType.Excel, pane);
    }

    private void createJdbcPane(Composite parent) {

        final Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        pane.setLayout(createPaneLayout());
        Group inputTypeGroup = new Group(pane, SWT.NONE);
        inputTypeGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        inputTypeGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));

        rbParams = new Button(inputTypeGroup, SWT.RADIO);
        rbParams.setSelection(subpaneState == SUBPANE_PARAM_STATE);
        rbParams.setText(Localization.getString("datasource.dialog.button.label.useProperties"));
        rbUrl = new Button(inputTypeGroup, SWT.RADIO);
        rbUrl.setText(Localization.getString("datasource.dialog.button.label.useUrl"));
        rbUrl.setSelection(subpaneState == SUBPANE_URL_STATE);
        subPanes = new Composite(pane, SWT.FILL);
        GridData gdPanes = new GridData(GridData.FILL_BOTH);
        gdPanes.horizontalSpan = 2;
        subPanes.setLayoutData(gdPanes);

        rbUrl.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.getSource();
                if (button.getSelection()) {
                    updateSubpane(subPanes, SUBPANE_URL_STATE);
                }
            }
        });
        rbParams.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.getSource();
                if (button.getSelection()) {
                    updateSubpane(subPanes, SUBPANE_PARAM_STATE);
                }
            }
        });

        subPanes.setLayout(stackLayoutDetail);
        createUrlJdbcSubPane(subPanes);
        createParamJdbcSubPane(subPanes);
        stackLayoutDetail.topControl = paneCacheDetail[subpaneState];
        paneCache.put(DataSourceType.JDBC, pane);
    }

    private void createUrlJdbcSubPane(Composite parent) {
        Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        pane.setLayout(createPaneLayout());
        Label label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.dbUrl") + colon);
        txtDbUrl = new Text(pane, SWT.BORDER);
        txtDbUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        txtDbUrl.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateButtons();
            }
        });
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
        StringJoiner sj = new StringJoiner("\n");
        for (JdbcDataSourceType dsType : JdbcDataSourceType.values()) {
            sj.add(dsType.urlSample());
        }
        label.setText(sj.toString());
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.userName") + colon);
        txtUrlUserName = new Text(pane, SWT.BORDER);
        txtUrlUserName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.password") + colon);
        txtUrlPassword = new Text(pane, SWT.BORDER | SWT.PASSWORD);
        txtUrlPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        paneCacheDetail[SUBPANE_URL_STATE] = pane;
    }

    private void createParamJdbcSubPane(Composite parent) {
        Composite pane = new Composite(parent, SWT.FILL);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
                updateButtons();
            }
        });
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.host") + colon);
        txtDbHost = new Text(pane, SWT.BORDER);
        txtDbHost.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.port") + colon);
        txtDbHostPort = new Text(pane, SWT.BORDER);
        txtDbHostPort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.dbName") + colon);
        txtDbName = new Text(pane, SWT.BORDER);
        txtDbName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.userName") + colon);
        txtParamUserName = new Text(pane, SWT.BORDER);
        txtParamUserName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label = new Label(pane, SWT.NONE);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        label.setText(Localization.getString("datasource.property.password") + colon);
        txtParamPassword = new Text(pane, SWT.BORDER | SWT.PASSWORD);
        txtParamPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        paneCacheDetail[SUBPANE_PARAM_STATE] = pane;
    }

    private void updateSubpane(Composite panes, int state) {
        subpaneState = state;
        stackLayoutDetail.topControl = paneCacheDetail[subpaneState];
        panes.layout();
        updateButtons();
    }

    private void updateStateButtons() {
        rbParams.setSelection(subpaneState == SUBPANE_PARAM_STATE);
        rbUrl.setSelection(subpaneState == SUBPANE_URL_STATE);
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
                    break;
                case JDBC:
                    String dbType = root.elementTextTrim(ELEMENT_DB_TYPE);
                    if (Strings.isNullOrEmpty(dbType)) {
                        txtDbHost.setText("");
                        txtDbHostPort.setText("");
                        cbDbType.setText("");
                        txtDbName.setText("");
                        txtDbUrl.setText(root.element(ELEMENT_DB_URL).getText());
                        txtUrlUserName.setText(Strings.nullToEmpty(root.element(ELEMENT_USER_NAME).getText()));
                        txtUrlPassword.setText(Strings.nullToEmpty(root.elementText(ELEMENT_PASSWORD)));
                        updateSubpane(subPanes, SUBPANE_URL_STATE);
                        updateStateButtons();
                    } else {
                        String[] hostPort = parseJdbcShortUrl(root.element(ELEMENT_DB_URL).getText(), dbType);
                        txtDbHost.setText(hostPort[0]);
                        txtDbHostPort.setText(hostPort[1]);
                        cbDbType.setText(dbType);
                        txtDbName.setText(root.element(ELEMENT_DB_NAME).getText());
                        txtDbUrl.setText("");
                        txtParamUserName.setText(root.element(ELEMENT_USER_NAME).getText());
                        txtParamPassword.setText(Strings.nullToEmpty(root.elementText(ELEMENT_PASSWORD)));
                        updateSubpane(subPanes, SUBPANE_PARAM_STATE);
                        updateStateButtons();
                    }
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
            break;
        case JDBC:
            switch (subpaneState) {
            case SUBPANE_PARAM_STATE:
                dataSource.addElement(ELEMENT_DB_TYPE).addText(cbDbType.getText());
                dataSource.addElement(ELEMENT_DB_URL).addText(getJdbcShortUrl(txtDbHost.getText(), txtDbHostPort.getText(), cbDbType.getText()));
                dataSource.addElement(ELEMENT_DB_NAME).addText(txtDbName.getText());
                dataSource.addElement(ELEMENT_USER_NAME).addText(txtParamUserName.getText());
                if (!txtParamPassword.getText().equals("")) {
                    dataSource.addElement(ELEMENT_PASSWORD).addText(txtParamPassword.getText());
                }
                break;
            case SUBPANE_URL_STATE:
                dataSource.addElement(ELEMENT_DB_URL).addText(txtDbUrl.getText());
                dataSource.addElement(ELEMENT_USER_NAME).addText(txtUrlUserName.getText());
                if (!txtUrlPassword.getText().equals("")) {
                    dataSource.addElement(ELEMENT_PASSWORD).addText(txtUrlPassword.getText());
                }
                break;
            }
            break;
        default: // JNDI
            dataSource.addElement(ELEMENT_JNDI_NAME).addText(txtJndiName.getText());
        }
        return XmlUtil.toString(document);
    }

    private String getJdbcShortUrl(String host, String port, String dbType) {
        final String portWc = "PORT=";
        final String hostWc = "<HOST>";
        String urlSample = JdbcDataSourceType.valueOf(dbType).urlSample();
        int portStartIdx = urlSample.indexOf(portWc);
        int portEndIdx = urlSample.indexOf(">", portStartIdx);
        String defaultPort = urlSample.substring(portStartIdx + portWc.length(), portEndIdx);
        String url = urlSample.substring(0, urlSample.lastIndexOf("<", portStartIdx)).replaceAll(hostWc, (host.equals("") ? "localhost" : host))
                + (port.equals("") ? defaultPort : port);
        return url;
    }

    private String[] parseJdbcShortUrl(String url, String dbType) {
        final String portWc = "PORT=";
        final String hostWc = "<HOST>";
        String[] result = new String[2];
        String urlSample = JdbcDataSourceType.valueOf(dbType).urlSample();
        int portStartIdx = urlSample.indexOf(portWc);
        int portEndIdx = urlSample.indexOf(">", portStartIdx);
        String defaultPort = urlSample.substring(portStartIdx + portWc.length(), portEndIdx);
        int hostStartIdx = urlSample.indexOf(hostWc);
        int splitterIdx = url.indexOf(":", hostStartIdx);
        if (splitterIdx > -1) {
            result[0] = url.substring(hostStartIdx, splitterIdx).trim();
            if (splitterIdx < url.length() - 1) {
                result[1] = url.substring(splitterIdx + 1);
            } else {
                result[1] = defaultPort;
            }
        } else {
            result[0] = url.substring(hostStartIdx).trim();
            result[1] = defaultPort;
        }
        return result;
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
        Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
        cancelButton.setText(Localization.getString("button.cancel"));
    }

    private void updateButtons() {
        Button btnOk = getButton(IDialogConstants.OK_ID);
        if (btnOk != null) {
            String name = txtName.getText().trim();
            boolean canBeSaved = name.length() > 0
                    && (ds != null && ds.getName().equals(name + DATA_SOURCE_FILE_SUFFIX)
                            || !DataSourceUtils.getDataSourcesProject().getFile(name + DATA_SOURCE_FILE_SUFFIX).exists())
                    && cbType.getText().length() > 0
                    && (!DataSourceType.valueOf(cbType.getText()).equals(DataSourceType.JDBC)
                            || (DataSourceType.valueOf(cbType.getText()).equals(DataSourceType.JDBC) && (subpaneState == SUBPANE_PARAM_STATE)
                                    && (cbDbType.getText().length() > 0))
                            || (DataSourceType.valueOf(cbType.getText()).equals(DataSourceType.JDBC) && (subpaneState == SUBPANE_URL_STATE)
                                    && (txtDbUrl.getText().length() > 0)));
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

    private final int SUBPANE_PARAM_STATE = 0;
    private final int SUBPANE_URL_STATE = 1;

}

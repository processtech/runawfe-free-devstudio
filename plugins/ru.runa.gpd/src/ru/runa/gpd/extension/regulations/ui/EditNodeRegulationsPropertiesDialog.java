package ru.runa.gpd.extension.regulations.ui;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
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
import org.osgi.framework.Bundle;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.NodeRegulationsProperties;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EditNodeRegulationsPropertiesDialog extends Dialog {
    private static boolean ERROR_ABOUT_BROWSER_LOGGED = false;
    private final Node node;
    private final NodeRegulationsProperties properties;
    private final List<Node> comboNodes = Lists.newArrayList();
    private final String[] comboLabels;
    private Button enabledCheckbox;
    private Label previousNodeLabel;
    private Label nextNodeLabel;
    private Combo previousNodeCombo;
    private Combo nextNodeCombo;
    private Label descriptionLabel;
    private Browser browser;
    // used if browser creation fails
    private Text text;

    public EditNodeRegulationsPropertiesDialog(Node node) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.node = node;
        this.properties = node.getRegulationsProperties().getCopy();
        ProcessDefinition processDefinition = node.getProcessDefinition();
        List<Node> allNodes = processDefinition.getChildren(Node.class);
        allNodes.remove(node);
        comboNodes.add(null);
        for (Node anotherNode : allNodes) {
            if (anotherNode.getRegulationsProperties().isEnabled()) {
                comboNodes.add(anotherNode);
            }
        }
        Collections.sort(comboNodes);
        List<String> labels = Lists.transform(comboNodes, new Function<Node, String>() {

            @Override
            public String apply(Node node) {
                if (node == null) {
                    return Localization.getString("EditNodeRegulationsPropertiesDialog.text.notSet");
                }
                return node.getName() + " [" + node.getId() + "]";
            }
        });
        this.comboLabels = labels.toArray(new String[labels.size()]);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("EditNodeRegulationsPropertiesDialog.title", node.getName()));
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(displayRectangle.width - (displayRectangle.width / 100 * 30), displayRectangle.height - (displayRectangle.height / 100 * 25));
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
        shell.setMinimumSize(640, 480);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        parent.setLayout(new GridLayout(1, false));

        Composite enabledComposite = new Composite(parent, SWT.NONE);
        Composite nodesComposite = new Composite(parent, SWT.NONE);
        Composite descriptionComposite = new Composite(parent, SWT.NONE);

        enabledComposite.setLayout(new GridLayout(1, false));
        nodesComposite.setLayout(new GridLayout(2, false));
        descriptionComposite.setLayout(new GridLayout(1, false));

        GridData dateGridData4 = new GridData(SWT.FILL, SWT.FILL, true, true);
        enabledCheckbox = new Button(enabledComposite, SWT.CHECK);
        enabledCheckbox.setText(Localization.getString("EditNodeRegulationsPropertiesDialog.enabledCheckbox.text"));
        enabledCheckbox.setLayoutData(dateGridData4);
        enabledCheckbox.setSelection(properties.isEnabled());
        enabledCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                properties.setEnabled(enabledCheckbox.getSelection());
                updateButtons();
            }
        });

        GridData dateGridData0 = new GridData(SWT.FILL, SWT.NONE, true, false);
        nodesComposite.setLayoutData(dateGridData0);

        previousNodeLabel = new Label(nodesComposite, SWT.NONE);
        previousNodeLabel.setText(Localization.getString("EditNodeRegulationsPropertiesDialog.previousNodeLabel.text"));
        previousNodeCombo = new Combo(nodesComposite, SWT.READ_ONLY);
        previousNodeCombo.setItems(comboLabels);
        if (properties.getPreviousNode() != null) {
            previousNodeCombo.select(comboNodes.indexOf(properties.getPreviousNode()));
        }
        GridData dateGridData1 = new GridData(SWT.FILL, SWT.NONE, false, true);
        GridData dateGridData2 = new GridData(SWT.FILL, SWT.NONE, true, true);
        dateGridData2.widthHint = 600;
        dateGridData2.minimumHeight = previousNodeCombo.getSize().y;
        GC gc = new GC(previousNodeLabel);
        dateGridData1.minimumWidth = gc.stringExtent(previousNodeLabel.getText()).x;
        previousNodeLabel.setLayoutData(dateGridData1);
        previousNodeCombo.setLayoutData(dateGridData2);

        previousNodeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                properties.setPreviousNode(comboNodes.get(previousNodeCombo.getSelectionIndex()));
                updateButtons();
            }
        });

        nextNodeLabel = new Label(nodesComposite, SWT.NONE);
        nextNodeLabel.setText(Localization.getString("EditNodeRegulationsPropertiesDialog.nextNodeLabel.text"));
        nextNodeCombo = new Combo(nodesComposite, SWT.READ_ONLY);
        nextNodeCombo.setItems(comboLabels);
        if (properties.getNextNode() != null) {
            nextNodeCombo.select(comboNodes.indexOf(properties.getNextNode()));
        }
        GridData dateGridData3 = new GridData(SWT.FILL, SWT.NONE, false, true);
        gc = new GC(nextNodeLabel);
        dateGridData3.minimumWidth = gc.stringExtent(nextNodeLabel.getText()).x;
        gc.dispose();
        nextNodeLabel.setLayoutData(dateGridData3);
        nextNodeCombo.setLayoutData(dateGridData2);
        nextNodeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                properties.setNextNode(comboNodes.get(nextNodeCombo.getSelectionIndex()));
                updateButtons();
            }
        });

        descriptionLabel = new Label(descriptionComposite, SWT.NONE);
        descriptionLabel.setText(Localization.getString("EditNodeRegulationsPropertiesDialog.descriptionLabel.text"));
        GridData dateGridData5 = new GridData(SWT.FILL, SWT.FILL, true, true);
        descriptionComposite.setLayoutData(dateGridData5);
        GridData dateGridData6 = new GridData(SWT.FILL, SWT.NONE, false, false);
        descriptionLabel.setLayoutData(dateGridData6);
        GridData dateGridData7 = new GridData(SWT.FILL, SWT.FILL, true, true);
        dateGridData7.minimumHeight = 400;
        dateGridData7.heightHint = 400;
        try {
            browser = new Browser(descriptionComposite, SWT.NONE);
            Bundle bundle = Platform.getBundle("ru.runa.gpd.form.ftl");
            URL url = FileLocator.find(bundle, new Path("/CKeditor4/editor.html"), null);
            URL fileURL = FileLocator.resolve(url);
            String fileURLAsString = fileURL.toString().replace("file:/", "file:///");
            if (fileURLAsString.indexOf(".jar!") != -1) {
                String tillJar = fileURLAsString.substring(0, fileURLAsString.indexOf(".jar!"));
                fileURLAsString = tillJar.substring(4, tillJar.lastIndexOf("/")) + "/CKeditor4/editor.html";
            }
            browser.setUrl(fileURLAsString);
            browser.setLayoutData(dateGridData7);
            new GetHTMLCallbackFunction(browser);
            new OnLoadCallbackFunction(browser);
        } catch (Throwable th) {
            if (!ERROR_ABOUT_BROWSER_LOGGED) {
                PluginLogger.logErrorWithoutDialog("Unable to create browser", th);
                ERROR_ABOUT_BROWSER_LOGGED = true;
            }
            text = new Text(descriptionComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            text.setLayoutData(dateGridData7);
            text.setText(properties.getDescription());
            text.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    EditNodeRegulationsPropertiesDialog.this.properties.setDescription(text.getText());
                }
            });
        }
        return parent;
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {

        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            EditNodeRegulationsPropertiesDialog.this.properties.setDescription(((String) arguments[0]));
            return null;
        }
    }

    private class OnLoadCallbackFunction extends BrowserFunction {

        public OnLoadCallbackFunction(Browser browser) {
            super(browser, "onLoadCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            browser.execute("CKEDITOR.instances['editor'].setData('" + properties.getDescription() + "');");
            return null;
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(properties.isValid());
    }

    @Override
    protected void okPressed() {
        if (browser != null) {
            browser.execute("getHTML();");
        }
        node.setRegulationsProperties(properties);
        this.close();
    }
}

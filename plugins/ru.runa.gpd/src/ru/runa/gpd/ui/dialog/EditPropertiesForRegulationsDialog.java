package ru.runa.gpd.ui.dialog;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
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
import org.osgi.framework.Bundle;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.collect.Lists;

public class EditPropertiesForRegulationsDialog extends Dialog {
    private Label previousNodeLabel;
    private Label nextNodeLabel;
    private Combo previousNodeCombo;
    private Combo nextNodeCombo;
    private Button includeInRegulationsCheckbox;
    private Label descriptionLabel;
    private String descriptionTextAsString = "";
    private final Node formNode;
    private Browser browser;

    public EditPropertiesForRegulationsDialog(Node formNode) {
        super(Display.getCurrent().getActiveShell());
        this.formNode = formNode;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("EditPropertiesForRegulationsDialog.title", formNode.getName()));
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(displayRectangle.width - (displayRectangle.width / 100 * 30), displayRectangle.height - (displayRectangle.height / 100 * 25));
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
        shell.setMinimumSize(640, 480);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        parent.setLayout(new GridLayout(1, false));

        Composite composite0 = new Composite(parent, SWT.NONE);
        Composite composite1 = new Composite(parent, SWT.NONE);
        Composite composite2 = new Composite(parent, SWT.NONE);

        GridLayout gl0 = new GridLayout(2, false);
        GridLayout gl1 = new GridLayout(1, false);
        GridLayout gl2 = new GridLayout(1, false);

        composite0.setLayout(gl0);
        composite1.setLayout(gl1);
        composite2.setLayout(gl2);

        GridData dateGridData0 = new GridData(SWT.FILL, SWT.NONE, true, false);
        composite0.setLayoutData(dateGridData0);

        previousNodeLabel = new Label(composite0, SWT.NONE);
        previousNodeLabel.setText(Localization.getString("EditPropertiesForRegulationsDialog.previousNodeLabel.text"));
        previousNodeCombo = new Combo(composite0, SWT.READ_ONLY);
        ProcessDefinition processDefinition = formNode.getProcessDefinition();
        List<Node> elementsList = processDefinition.getChildren(Node.class);
        String[] previousNodesListItems = { Localization.getString("EditPropertiesForRegulationsDialog.previousNodeCombo.text.notSet") };
        List<String> previousNodesListItemsAsListOnlyNames = Lists.newArrayList();
        List<String> previousNodesListItemsAsList = Lists.newArrayList();
        for (Node node : elementsList) {
            String id = node.getId();
            if (id.equals(formNode.getId()) != true) {
                previousNodesListItemsAsListOnlyNames.add(node.getName());
                previousNodesListItemsAsList.add(node.getName() + " [" + node.getId() + "]");
            }
        }
        Collections.sort(previousNodesListItemsAsListOnlyNames);
        Collections.sort(previousNodesListItemsAsList);
        for (String name : previousNodesListItemsAsList) {
            previousNodesListItems = Arrays.copyOf(previousNodesListItems, previousNodesListItems.length + 1);
            previousNodesListItems[previousNodesListItems.length - 1] = name;
        }
        previousNodeCombo.setItems(previousNodesListItems);
        int previousNodePositionInList = -1;
        if (formNode.getNodeRegulationsProperties().getPreviousNode() != null) {
            previousNodePositionInList = previousNodesListItemsAsListOnlyNames.indexOf(((Node) formNode.getNodeRegulationsProperties()
                    .getPreviousNode()).getName()) + 1;
        }
        if (previousNodePositionInList != -1) {
            previousNodeCombo.select(previousNodePositionInList);
        } else {
            previousNodeCombo.select(0);
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
                updateButtons();
            }
        });

        nextNodeLabel = new Label(composite0, SWT.NONE);
        nextNodeLabel.setText(Localization.getString("EditPropertiesForRegulationsDialog.nextNodeLabel.text"));
        nextNodeCombo = new Combo(composite0, SWT.READ_ONLY);
        String[] nextNodesListItems = { Localization.getString("EditPropertiesForRegulationsDialog.nextNodeCombo.text.notSet") };
        List<String> nextNodesListItemsAsListOnlyNames = Lists.newArrayList();
        List<String> nextNodesListItemsAsList = Lists.newArrayList();
        for (Node node : elementsList) {
            String id = node.getId();
            if (id.equals(formNode.getId()) != true) {
                nextNodesListItemsAsListOnlyNames.add(node.getName());
                nextNodesListItemsAsList.add(node.getName() + " [" + node.getId() + "]");
            }
        }
        Collections.sort(nextNodesListItemsAsListOnlyNames);
        Collections.sort(nextNodesListItemsAsList);
        for (String name : nextNodesListItemsAsList) {
            nextNodesListItems = Arrays.copyOf(nextNodesListItems, nextNodesListItems.length + 1);
            nextNodesListItems[nextNodesListItems.length - 1] = name;
        }
        nextNodeCombo.setItems(nextNodesListItems);
        int nextNodePositionInList = -1;
        if (formNode.getNodeRegulationsProperties().getNextNode() != null) {
            nextNodePositionInList = nextNodesListItemsAsListOnlyNames.indexOf(((Node) formNode.getNodeRegulationsProperties().getNextNode())
                    .getName()) + 1;
        }
        if (nextNodePositionInList != -1) {
            nextNodeCombo.select(nextNodePositionInList);
        } else {
            nextNodeCombo.select(0);
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
                updateButtons();
            }
        });

        GridData dateGridData4 = new GridData(SWT.FILL, SWT.FILL, true, true);
        includeInRegulationsCheckbox = new Button(composite1, SWT.CHECK);
        includeInRegulationsCheckbox.setText(Localization.getString("EditPropertiesForRegulationsDialog.includeInRegulationsCheckbox.text"));
        includeInRegulationsCheckbox.setLayoutData(dateGridData4);
        includeInRegulationsCheckbox.setSelection(formNode.getNodeRegulationsProperties().getIsEnabled());
        includeInRegulationsCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateButtons();
            }
        });

        descriptionLabel = new Label(composite2, SWT.NONE);
        descriptionLabel.setText(Localization.getString("EditPropertiesForRegulationsDialog.descriptionLabel.text"));
        GridData dateGridData5 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite2.setLayoutData(dateGridData5);
        GridData dateGridData6 = new GridData(SWT.FILL, SWT.NONE, false, false);
        descriptionLabel.setLayoutData(dateGridData6);
        try {
            GridData dateGridData7 = new GridData(SWT.FILL, SWT.FILL, true, true);
            browser = new Browser(composite2, SWT.NONE);
            Bundle bundle = Platform.getBundle("ru.runa.gpd.form.ftl");
            URL url = FileLocator.find(bundle, new Path("/CKeditor4/editor.html"), null);
            URL fileURL = FileLocator.resolve(url);
            String fileURLAsString = fileURL.toString().replace("file:/", "file:///");
            if (fileURLAsString.indexOf(".jar!") != -1) {
                String tillJar = fileURLAsString.substring(0, fileURLAsString.indexOf(".jar!"));
                fileURLAsString = tillJar.substring(4, tillJar.lastIndexOf("/")) + "/CKeditor4/editor.html";
            }
            browser.setUrl(fileURLAsString);
            dateGridData7.minimumHeight = 400;
            dateGridData7.heightHint = 400;
            browser.setLayoutData(dateGridData7);
            new GetHTMLCallbackFunction(browser, this.descriptionTextAsString);
            new OnLoadCallbackFunction(browser, formNode.getNodeRegulationsProperties().getDescriptionForUser());
        } catch (IOException e) {
            PluginLogger.logErrorWithoutDialog("FileLocator.toFileURL() failed.", e);
        }

        return parent;
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {

        public GetHTMLCallbackFunction(Browser browser, String text) {
            super(browser, "getHTMLCallback");
            descriptionTextAsString = text;
        }

        @Override
        public Object function(Object[] arguments) {
            descriptionTextAsString = ((String) arguments[0]);
            return null;
        }
    }

    private class OnLoadCallbackFunction extends BrowserFunction {
        private String htmlToSet = "";

        public OnLoadCallbackFunction(Browser browser, String htmlToSet) {
            super(browser, "onLoadCallback");
            this.htmlToSet = htmlToSet;
        }

        @Override
        public Object function(Object[] arguments) {
            browser.execute("CKEDITOR.instances['editor'].setData('" + htmlToSet.replaceAll("\n", "") + "');");
            return null;
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID)
                .setEnabled(
                        (previousNodeCombo.getSelectionIndex() == 0 && nextNodeCombo.getSelectionIndex() == 0 && includeInRegulationsCheckbox
                                .getSelection()) != true);
    }

    private Node getNodeByName(String name) {
        Node result = null;
        ProcessDefinition processDefinition = formNode.getProcessDefinition();
        List<Node> elementsList = processDefinition.getChildren(Node.class);
        for (Node node : elementsList) {
            String nodeName = node.getName();
            if (nodeName.equals(name)) {
                result = node;
                break;
            }
        }
        return result;
    }

    @Override
    protected void okPressed() {
        Pattern pattern = Pattern.compile("\\[([A-Za-z0-9_]*\\.)*ID[0-9]+\\]$");
        Matcher matcher;
        if (previousNodeCombo.getSelectionIndex() > 0) {
            matcher = pattern.matcher(previousNodeCombo.getText());
            matcher.find();
            String previousNameForSearch = previousNodeCombo.getText().substring(0, previousNodeCombo.getText().indexOf(matcher.group(0)) - 1);
            Node newPreviousNode = getNodeByName(previousNameForSearch);
            formNode.getNodeRegulationsProperties().setPreviousNode(newPreviousNode);
        } else {
            formNode.getNodeRegulationsProperties().setPreviousNode(null);
        }
        if (nextNodeCombo.getSelectionIndex() > 0) {
            matcher = pattern.matcher(nextNodeCombo.getText());
            matcher.find();
            String nextNameForSearch = nextNodeCombo.getText().substring(0, nextNodeCombo.getText().indexOf(matcher.group(0)) - 1);
            Node newNextNode = getNodeByName(nextNameForSearch);
            formNode.getNodeRegulationsProperties().setNextNode(newNextNode);
        } else {
            formNode.getNodeRegulationsProperties().setNextNode(null);
        }
        formNode.getNodeRegulationsProperties().setIsEnabled(includeInRegulationsCheckbox.getSelection());
        browser.execute("getHTML();");
        formNode.getNodeRegulationsProperties().setDescriptionForUser(descriptionTextAsString);
        formNode.getProcessDefinition().setDirty();
        this.close();
    }
}

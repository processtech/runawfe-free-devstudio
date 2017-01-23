package ru.runa.gpd.ui.dialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.collect.Lists;

public class EditPropertiesForRegulationDialog extends Dialog {
    private Label previousNodeLabel;
    private Label nextNodeLabel;
    private Combo previousNodeCombo;
    private Combo nextNodeCombo;
    private Button includeInRegulationCheckbox;
    private Label descriptionLabel;
    private Text descriptionText;
    private final Node formNode;

    public EditPropertiesForRegulationDialog(Node formNode) {
        super(Display.getCurrent().getActiveShell());
        this.formNode = formNode;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Localization.getString("EditPropertiesForRegulationDialog.title", formNode.getName()));
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(displayRectangle.width - (displayRectangle.width / 100 * 55), displayRectangle.height - (displayRectangle.height / 100 * 50));
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
        shell.setMinimumSize(540, 380);

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        parent.setLayout(new GridLayout(1, false));
        Composite composite0 = new Composite(parent, SWT.NONE);
        Composite composite1 = new Composite(parent, SWT.NONE);
        composite0.setLayout(new GridLayout(2, false));
        composite1.setLayout(new GridLayout(1, false));
        GridData dateGridData0 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite0.setLayoutData(dateGridData0);

        previousNodeLabel = new Label(composite0, SWT.NONE);
        previousNodeLabel.setText(Localization.getString("EditPropertiesForRegulationDialog.previousNodeLabel.text"));
        previousNodeCombo = new Combo(composite0, SWT.READ_ONLY);
        ProcessDefinition processDefinition = formNode.getProcessDefinition();
        List<Node> elementsList = processDefinition.getChildren(Node.class);
        String[] previousNodesListItems = { Localization.getString("EditPropertiesForRegulationDialog.previousNodeCombo.text.notSet") };
        List<String> previousNodesListItemsAsList = Lists.newArrayList();
        for (Node node : elementsList) {
            String id = node.getId();
            if (id.equals(formNode.getId()) != true) {
                previousNodesListItemsAsList.add(node.getName());
            }
        }
        Collections.sort(previousNodesListItemsAsList);
        for (String name : previousNodesListItemsAsList) {
            previousNodesListItems = Arrays.copyOf(previousNodesListItems, previousNodesListItems.length + 1);
            previousNodesListItems[previousNodesListItems.length - 1] = name;
        }
        previousNodeCombo.setItems(previousNodesListItems);
        int previousNodePositionInList = -1;
        if (formNode.getPreviousNodeInRegulation() != null) {
            previousNodePositionInList = previousNodesListItemsAsList.indexOf(((Node) formNode.getPreviousNodeInRegulation()).getName()) + 1;
        }
        if (previousNodePositionInList != -1) {
            previousNodeCombo.select(previousNodePositionInList);
        } else {
            previousNodeCombo.select(0);
        }
        GridData dateGridData1 = new GridData(SWT.FILL, SWT.NONE, true, true);
        previousNodeLabel.setLayoutData(dateGridData1);
        previousNodeCombo.setLayoutData(dateGridData1);
        previousNodeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateButtons();
            }
        });

        nextNodeLabel = new Label(composite0, SWT.NONE);
        nextNodeLabel.setText(Localization.getString("EditPropertiesForRegulationDialog.nextNodeLabel.text"));
        nextNodeCombo = new Combo(composite0, SWT.READ_ONLY);
        String[] nextNodesListItems = { Localization.getString("EditPropertiesForRegulationDialog.nextNodeCombo.text.notSet") };
        List<String> nextNodesListItemsAsList = Lists.newArrayList();
        for (Node node : elementsList) {
            String id = node.getId();
            if (id.equals(formNode.getId()) != true) {
                nextNodesListItemsAsList.add(node.getName());
            }
        }
        Collections.sort(nextNodesListItemsAsList);
        for (String name : nextNodesListItemsAsList) {
            nextNodesListItems = Arrays.copyOf(nextNodesListItems, nextNodesListItems.length + 1);
            nextNodesListItems[nextNodesListItems.length - 1] = name;
        }
        nextNodeCombo.setItems(nextNodesListItems);
        int nextNodePositionInList = -1;
        if (formNode.getNextNodeInRegulation() != null) {
            nextNodePositionInList = nextNodesListItemsAsList.indexOf(((Node) formNode.getNextNodeInRegulation()).getName()) + 1;
        }
        if (nextNodePositionInList != -1) {
            nextNodeCombo.select(nextNodePositionInList);
        } else {
            nextNodeCombo.select(0);
        }
        GridData dateGridData2 = new GridData(SWT.FILL, SWT.NONE, true, true);
        nextNodeCombo.setLayoutData(dateGridData2);
        nextNodeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateButtons();
            }
        });

        includeInRegulationCheckbox = new Button(composite0, SWT.CHECK);
        includeInRegulationCheckbox.setText(Localization.getString("EditPropertiesForRegulationDialog.includeInRegulationCheckbox.text"));
        includeInRegulationCheckbox.setLayoutData(dateGridData2);
        includeInRegulationCheckbox.setSelection(formNode.getIsEnabledInRegulation());
        includeInRegulationCheckbox.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateButtons();
            }
        });

        descriptionLabel = new Label(composite1, SWT.NONE);
        descriptionLabel.setText(Localization.getString("EditPropertiesForRegulationDialog.descriptionLabel.text"));
        GridData dateGridData5 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite1.setLayoutData(dateGridData5);
        GridData dateGridData6 = new GridData(SWT.FILL, SWT.NONE, false, false);
        descriptionLabel.setLayoutData(dateGridData6);
        descriptionText = new Text(composite1, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData dateGridData7 = new GridData(SWT.FILL, SWT.FILL, true, true);
        dateGridData7.heightHint = descriptionText.getLineHeight() * 12;
        descriptionText.setLayoutData(dateGridData7);
        descriptionText.setText(formNode.getDescriptionForUserInRegulation());

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID)
                .setEnabled(
                        (previousNodeCombo.getSelectionIndex() == 0 && nextNodeCombo.getSelectionIndex() == 0 && includeInRegulationCheckbox
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
        Node newPreviousNode = getNodeByName(previousNodeCombo.getText());
        formNode.setPreviousNodeInRegulation(newPreviousNode);
        Node newNextNode = getNodeByName(nextNodeCombo.getText());
        formNode.setNextNodeInRegulation(newNextNode);
        formNode.setIsEnabledInRegulation(includeInRegulationCheckbox.getSelection());
        formNode.setDescriptionForUserInRegulation(descriptionText.getText());
        formNode.getProcessDefinition().setDirty();
        this.close();
    }
}

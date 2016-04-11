package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class CopyFormDialog extends Dialog {
    private Combo statesCombo;
    private final List<FormNode> formNodes = Lists.newArrayList();
    private FormNode selectedFormNode;

    public CopyFormDialog(FormNode currentNode) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        ProcessDefinition mainProcessDefinition = currentNode.getProcessDefinition().getMainProcessDefinition();
        fetchFormNodes(mainProcessDefinition, currentNode);
        for (SubprocessDefinition subprocessDefinition : mainProcessDefinition.getEmbeddedSubprocesses().values()) {
            fetchFormNodes(subprocessDefinition, currentNode);
        }
    }

    private void fetchFormNodes(ProcessDefinition processDefinition, FormNode currentNode) {
        for (FormNode node : processDefinition.getChildren(FormNode.class)) {
            if (!Objects.equal(node, currentNode) && node.hasForm()) {
                formNodes.add(node);
            }
        }
    }

    @Override
    public Control createDialogArea(final Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout());

        Composite composite = new Composite(area, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        statesCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        statesCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
        for (FormNode formNode : formNodes) {
            if (formNode.getProcessDefinition() instanceof SubprocessDefinition) {
                statesCombo.add(formNode.getProcessDefinition().getId() + "." + formNode.getName());
            } else {
                statesCombo.add(formNode.getName());
            }
        }
        statesCombo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateVisibility();
            }
        });

        return area;
    }

    @Override
    protected void okPressed() {
        if (selectedFormNode != null) {
            super.okPressed();
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        if (formNodes.size() > 0) {
            statesCombo.select(0);
        }
        updateVisibility();
        return control;
    }

    private void updateVisibility() {
        int index = statesCombo.getSelectionIndex();
        if (index != -1) {
            selectedFormNode = formNodes.get(index);
        } else {
            selectedFormNode = null;
        }
        getButton(IDialogConstants.OK_ID).setEnabled(selectedFormNode != null);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("CopyFormDialog.selectState"));
    }

    public FormNode getSelectedFormNode() {
        return selectedFormNode;
    }

}

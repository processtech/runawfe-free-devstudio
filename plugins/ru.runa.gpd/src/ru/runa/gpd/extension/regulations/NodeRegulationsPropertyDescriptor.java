package ru.runa.gpd.extension.regulations;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.regulations.ui.EditNodeRegulationsPropertiesDialog;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.ui.custom.LoggingMouseAdapter;

public class NodeRegulationsPropertyDescriptor extends PropertyDescriptor {
    private final Node node;
    private Label enabledLabel;
    private Label previousNodeLabel;
    private Label nextNodeLabel;

    public NodeRegulationsPropertyDescriptor(Node node) {
        super(PropertyNames.PROPERTY_NODE_IN_REGULATIONS, Localization.getString("Node.property.nodeInRegulations"));
        this.node = node;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new NodeInRegulationsDialogCellEditor(parent);
    }

    public class NodeInRegulationsDialogCellEditor extends DialogCellEditor {
        private Composite editor;

        public NodeInRegulationsDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            Object result = null;
            EditNodeRegulationsPropertiesDialog dialog = new EditNodeRegulationsPropertiesDialog(node);
            int dialogResult = dialog.open();
            if (dialogResult == IDialogConstants.OK_ID) {
                result = node.getRegulationsProperties();
            }
            return result;
        }

        @Override
        protected Control createControl(Composite parent) {
            editor = new Composite(parent, getStyle());
            editor.setFont(parent.getFont());
            editor.setBackground(parent.getBackground());
            GridLayout layout = new GridLayout(3, false);
            layout.marginHeight = 0;
            editor.setLayout(layout);
            GridData gridDataLabelForCheckbox = new GridData(SWT.NONE, SWT.NONE, false, false);
            GridData gridDataLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
            enabledLabel = new Label(editor, SWT.NONE);
            enabledLabel.setFont(editor.getFont());
            enabledLabel.setBackground(editor.getBackground());
            enabledLabel.setLayoutData(gridDataLabelForCheckbox);

            previousNodeLabel = new Label(editor, SWT.NONE);
            previousNodeLabel.setFont(editor.getFont());
            previousNodeLabel.setBackground(editor.getBackground());
            previousNodeLabel.setLayoutData(gridDataLabel);

            nextNodeLabel = new Label(editor, SWT.NONE);
            nextNodeLabel.setFont(editor.getFont());
            nextNodeLabel.setBackground(editor.getBackground());
            nextNodeLabel.setLayoutData(gridDataLabel);

            LoggingMouseAdapter loggingMouseAdapter = new LoggingMouseAdapter() {

                @Override
                protected void onMouseUp(MouseEvent e) throws Exception {
                    NodeRegulationsProperties newProperties = (NodeRegulationsProperties) NodeInRegulationsDialogCellEditor.this
                            .openDialogBox(NodeInRegulationsDialogCellEditor.this.editor);
                    if (newProperties != null) {
                        NodeRegulationsPropertyDescriptor.this.node.setRegulationsProperties(newProperties);
                    }
                }
            };
            previousNodeLabel.addMouseListener(loggingMouseAdapter);
            nextNodeLabel.addMouseListener(loggingMouseAdapter);
            enabledLabel.addMouseListener(loggingMouseAdapter);
            editor.addMouseListener(loggingMouseAdapter);
            return editor;
        }

        @Override
        protected void updateContents(Object value) {
            NodeRegulationsProperties properties = (NodeRegulationsProperties) value;
            enabledLabel.setImage(SharedImages.getImage(properties.isEnabled() ? "icons/checked.gif" : "icons/unchecked.gif"));
            previousNodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations") + ": "
                    + RegulationsUtil.getNodeLabel(properties.getPreviousNode()));
            nextNodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations") + ": "
                    + RegulationsUtil.getNodeLabel(properties.getNextNode()));
        }

        @Override
        protected void doSetFocus() {
            // ignore
        }
    }

}

package ru.runa.gpd.extension.regulations;

import java.beans.PropertyChangeEvent;

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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.regulations.ui.EditNodeRegulationsPropertiesDialog;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.ui.custom.LoggingMouseAdapter;
import ru.runa.gpd.ui.view.PropertiesView;

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

        public Composite getEditor() {
            return editor;
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
            enabledLabel.setImage(SharedImages.getImage(node.getRegulationsProperties().isEnabled() ? "icons/checked.gif"
                    : "icons/unchecked.gif"));

            previousNodeLabel = new Label(editor, SWT.NONE);
            previousNodeLabel.setFont(editor.getFont());
            previousNodeLabel.setBackground(editor.getBackground());
            previousNodeLabel.setLayoutData(gridDataLabel);

            nextNodeLabel = new Label(editor, SWT.NONE);
            nextNodeLabel.setFont(editor.getFont());
            nextNodeLabel.setBackground(editor.getBackground());
            nextNodeLabel.setLayoutData(gridDataLabel);

            if (node.getRegulationsProperties().getPreviousNode() != null) {
                previousNodeLabel.setText(node.getRegulationsProperties().getPreviousNode().getLabel());
            } else {
                previousNodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations.notSet"));
            }

            if (node.getRegulationsProperties().getNextNode() != null) {
                nextNodeLabel.setText(node.getRegulationsProperties().getNextNode().getLabel());
            } else {
                nextNodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations.notSet"));
            }

            LoggingMouseAdapter loggingMouseAdapter = new LoggingMouseAdapter() {
                @Override
                protected void onMouseUp(MouseEvent e) throws Exception {
                    NodeRegulationsProperties newNodeRegulationProperties = (NodeRegulationsProperties) NodeInRegulationsDialogCellEditor.this
                            .openDialogBox(NodeInRegulationsDialogCellEditor.this.getEditor());
                    if (newNodeRegulationProperties != null) {
                        NodeRegulationsPropertyDescriptor.this.node.setRegulationsProperties(newNodeRegulationProperties);
                        PropertyChangeEvent eventNode;
                        eventNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_NODE_IN_REGULATIONS, null, newNodeRegulationProperties);
                        IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.ID);
                        if (viewPart instanceof PropertiesView) {
                            PropertiesView propertiesView = (PropertiesView) viewPart;
                            // TODO 2797 для чего?
                            propertiesView.propertyChange(eventNode);
                        }
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
            NodeRegulationsProperties newProperties;
            if (previousNodeLabel == null || nextNodeLabel == null) {
                return;
            }
            if (value != null) {
                newProperties = (NodeRegulationsProperties) value;
                node.setRegulationsProperties(newProperties);
                enabledLabel.setImage(SharedImages.getImage(newProperties.isEnabled() ? "icons/checked.gif" : "icons/unchecked.gif"));
                if (newProperties.getPreviousNode() != null) {
                    previousNodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations") + ": " + "["
                            + newProperties.getPreviousNode().getId() + "] " + ((Node) newProperties.getPreviousNode()).getName());
                } else {
                    previousNodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations") + ": "
                            + Localization.getString("Node.property.previousNodeInRegulations.notSet"));
                }
                if (newProperties.getNextNode() != null) {
                    nextNodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations") + ": " + "["
                            + newProperties.getNextNode().getId() + "] " + ((Node) newProperties.getNextNode()).getName());
                } else {
                    nextNodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations") + ": "
                            + Localization.getString("Node.property.nextNodeInRegulations.notSet"));
                }
            }
        }
    }
}

package ru.runa.gpd.property;

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
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.NodeRegulationsProperties;
import ru.runa.gpd.ui.custom.LoggingMouseAdapter;
import ru.runa.gpd.ui.dialog.EditPropertiesForRegulationsDialog;
import ru.runa.gpd.ui.view.PropertiesView;

public class NodeInRegulationsPropertyDescriptor extends PropertyDescriptor {
    private final Node node;

    private Label nodeIsIncludedLabel;
    private Label previousNodeLabel;
    private Label nextNodeLabel;

    public NodeInRegulationsPropertyDescriptor(Node node) {
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
            EditPropertiesForRegulationsDialog dialog = new EditPropertiesForRegulationsDialog(node);
            int dialogResult = dialog.open();
            if (dialogResult == IDialogConstants.OK_ID) {
                result = node.getNodeRegulationsProperties();
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
            nodeIsIncludedLabel = new Label(editor, SWT.NONE);
            nodeIsIncludedLabel.setFont(editor.getFont());
            nodeIsIncludedLabel.setBackground(editor.getBackground());
            nodeIsIncludedLabel.setLayoutData(gridDataLabelForCheckbox);
            nodeIsIncludedLabel.setImage(SharedImages.getImage(node.getNodeRegulationsProperties().getIsEnabled() ? "icons/checked.gif"
                    : "icons/unchecked.gif"));

            previousNodeLabel = new Label(editor, SWT.NONE);
            previousNodeLabel.setFont(editor.getFont());
            previousNodeLabel.setBackground(editor.getBackground());
            previousNodeLabel.setLayoutData(gridDataLabel);

            nextNodeLabel = new Label(editor, SWT.NONE);
            nextNodeLabel.setFont(editor.getFont());
            nextNodeLabel.setBackground(editor.getBackground());
            nextNodeLabel.setLayoutData(gridDataLabel);

            if (node.getNodeRegulationsProperties().getPreviousNode() != null) {
                previousNodeLabel.setText(node.getNodeRegulationsProperties().getPreviousNode().getLabel());
            } else {
                previousNodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations.notSet"));
            }

            if (node.getNodeRegulationsProperties().getNextNode() != null) {
                nextNodeLabel.setText(node.getNodeRegulationsProperties().getNextNode().getLabel());
            } else {
                nextNodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations.notSet"));
            }

            LoggingMouseAdapter loggingMouseAdapter = new LoggingMouseAdapter() {
                @Override
                protected void onMouseUp(MouseEvent e) throws Exception {
                    NodeRegulationsProperties newNodeRegulationProperties = (NodeRegulationsProperties) NodeInRegulationsDialogCellEditor.this
                            .openDialogBox(NodeInRegulationsDialogCellEditor.this.getEditor());
                    if (newNodeRegulationProperties != null) {
                        NodeInRegulationsPropertyDescriptor.this.node.setNodeRegulationsProperties(newNodeRegulationProperties);

                        PropertyChangeEvent eventNode;

                        eventNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_NODE_IN_REGULATIONS, null, newNodeRegulationProperties);

                        IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.ID);
                        if (viewPart instanceof PropertiesView) {
                            PropertiesView propertiesView = (PropertiesView) viewPart;
                            propertiesView.propertyChange(eventNode);
                        }
                    }
                }

                @Override
                protected void onMouseDown(MouseEvent e) throws Exception {
                }

                @Override
                protected void onMouseDoubleClick(MouseEvent e) throws Exception {

                }
            };
            previousNodeLabel.addMouseListener(loggingMouseAdapter);
            nextNodeLabel.addMouseListener(loggingMouseAdapter);
            nodeIsIncludedLabel.addMouseListener(loggingMouseAdapter);
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
                node.setNodeRegulationsProperties(newProperties);
                nodeIsIncludedLabel.setImage(SharedImages.getImage(newProperties.getIsEnabled() ? "icons/checked.gif" : "icons/unchecked.gif"));
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

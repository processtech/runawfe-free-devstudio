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
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.NodeRegulationsProperties;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.ui.custom.LoggingMouseAdapter;
import ru.runa.gpd.ui.dialog.EditPropertiesForRegulationsDialog;
import ru.runa.gpd.ui.view.PropertiesView;

public class NodeInRegulationsPropertyDescriptor extends PropertyDescriptor {
    public static final int PREVIOUS_NODE_MODE = 0;
    public static final int NEXT_NODE_MODE = 1;

    private final Node node;
    private final int mode;
    private Label nodeLabel;

    public NodeInRegulationsPropertyDescriptor(Object id, Node node, int mode) {
        super(id, mode == PREVIOUS_NODE_MODE ? Localization.getString("Node.property.previousNodeInRegulations") : Localization
                .getString("Node.property.nextNodeInRegulations"));
        this.node = node;
        this.mode = mode;
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
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            editor.setLayout(layout);
            nodeLabel = new Label(editor, SWT.NONE);
            GridData gridDataLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
            nodeLabel.setFont(editor.getFont());
            nodeLabel.setBackground(editor.getBackground());
            nodeLabel.setLayoutData(gridDataLabel);

            if (mode == PREVIOUS_NODE_MODE) {
                if (node.getNodeRegulationsProperties().getPreviousNode() != null) {
                    nodeLabel.setText(node.getNodeRegulationsProperties().getPreviousNode().getLabel());
                } else {
                    nodeLabel.setText(Localization.getString("Node.property.previousNodeInRegulations.notSet"));
                }
            } else {
                if (node.getNodeRegulationsProperties().getNextNode() != null) {
                    nodeLabel.setText(node.getNodeRegulationsProperties().getNextNode().getLabel());
                } else {
                    nodeLabel.setText(Localization.getString("Node.property.nextNodeInRegulations.notSet"));
                }
            }
            LoggingMouseAdapter loggingMouseAdapter = new LoggingMouseAdapter() {
                @Override
                protected void onMouseUp(MouseEvent e) throws Exception {
                    NodeRegulationsProperties newNodeRegulationProperties = (NodeRegulationsProperties) NodeInRegulationsDialogCellEditor.this
                            .openDialogBox(NodeInRegulationsDialogCellEditor.this.getEditor());
                    if (newNodeRegulationProperties != null) {
                        String oldPreviousNodeLabel = Localization.getString("Node.property.previousNodeInRegulations.notSet");
                        String oldNextNodeLabel = Localization.getString("Node.property.nextNodeInRegulations.notSet");
                        if (NodeInRegulationsPropertyDescriptor.this.node.getNodeRegulationsProperties().getPreviousNode() != null) {
                            oldPreviousNodeLabel = NodeInRegulationsPropertyDescriptor.this.node.getNodeRegulationsProperties().getPreviousNode()
                                    .getLabel();
                        }
                        if (NodeInRegulationsPropertyDescriptor.this.node.getNodeRegulationsProperties().getNextNode() != null) {
                            oldNextNodeLabel = NodeInRegulationsPropertyDescriptor.this.node.getNodeRegulationsProperties().getNextNode().getLabel();
                        }
                        NodeInRegulationsPropertyDescriptor.this.node.setNodeRegulationsProperties(newNodeRegulationProperties);

                        PropertyChangeEvent eventPreviousNode;
                        PropertyChangeEvent eventNextNode;
                        if (newNodeRegulationProperties.getPreviousNode() != null) {
                            eventPreviousNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_PREVIOUS_NODE_IN_REGULATIONS,
                                    oldPreviousNodeLabel, newNodeRegulationProperties.getPreviousNode().getLabel());
                        } else {
                            eventPreviousNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_PREVIOUS_NODE_IN_REGULATIONS,
                                    oldPreviousNodeLabel, Localization.getString("Node.property.previousNodeInRegulations.notSet"));
                        }
                        if (newNodeRegulationProperties.getNextNode() != null) {
                            eventNextNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_NEXT_NODE_IN_REGULATIONS, oldNextNodeLabel,
                                    newNodeRegulationProperties.getNextNode().getLabel());
                        } else {
                            eventNextNode = new PropertyChangeEvent(node, PropertyNames.PROPERTY_NEXT_NODE_IN_REGULATIONS, oldNextNodeLabel,
                                    Localization.getString("Node.property.nextNodeInRegulations.notSet"));
                        }
                        IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.ID);
                        if (viewPart instanceof PropertiesView) {
                            PropertiesView propertiesView = (PropertiesView) viewPart;
                            propertiesView.propertyChange(eventPreviousNode);
                            propertiesView.propertyChange(eventNextNode);
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
            nodeLabel.addMouseListener(loggingMouseAdapter);
            editor.addMouseListener(loggingMouseAdapter);
            return editor;
        }

        @Override
        protected void updateContents(Object value) {
            if (nodeLabel == null) {
                return;
            }

            String text = "";
            if (value != null) {
                text = value.toString();
            }
            nodeLabel.setText(text);
        }
    }
}

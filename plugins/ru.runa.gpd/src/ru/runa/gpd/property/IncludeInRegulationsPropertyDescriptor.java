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
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.NodeRegulationsProperties;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.ui.custom.LoggingMouseAdapter;
import ru.runa.gpd.ui.dialog.EditPropertiesForRegulationsDialog;
import ru.runa.gpd.ui.view.PropertiesView;

public class IncludeInRegulationsPropertyDescriptor extends PropertyDescriptor {
    private final Node node;
    private Label nodeLabel;

    public IncludeInRegulationsPropertyDescriptor(Object id, Node node) {
        super(id, Localization.getString("Node.property.includeInRegulations"));
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
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            editor.setLayout(layout);
            nodeLabel = new Label(editor, SWT.NONE);
            GridData gridDataLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
            nodeLabel.setFont(editor.getFont());
            nodeLabel.setBackground(editor.getBackground());
            nodeLabel.setLayoutData(gridDataLabel);
            nodeLabel.setImage(SharedImages
                    .getImage(node.getNodeRegulationsProperties().getIsEnabled() ? "icons/checked.gif" : "icons/unchecked.gif"));
            LoggingMouseAdapter loggingMouseAdapter = new LoggingMouseAdapter() {
                @Override
                protected void onMouseUp(MouseEvent e) throws Exception {
                    NodeRegulationsProperties newNodeRegulationProperties = (NodeRegulationsProperties) NodeInRegulationsDialogCellEditor.this
                            .openDialogBox(NodeInRegulationsDialogCellEditor.this.getEditor());
                    if (newNodeRegulationProperties != null) {
                        String oldIsEnabledValue = String.valueOf(IncludeInRegulationsPropertyDescriptor.this.node.getNodeRegulationsProperties()
                                .getIsEnabled());
                        IncludeInRegulationsPropertyDescriptor.this.node.setNodeRegulationsProperties(newNodeRegulationProperties);

                        PropertyChangeEvent eventIncludeInRegulations = new PropertyChangeEvent(node,
                                PropertyNames.PROPERTY_NODE_INCLUDE_IN_REGULATIONS, oldIsEnabledValue, String.valueOf(newNodeRegulationProperties
                                        .getIsEnabled()));

                        IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.ID);
                        if (viewPart instanceof PropertiesView) {
                            PropertiesView propertiesView = (PropertiesView) viewPart;
                            propertiesView.propertyChange(eventIncludeInRegulations);
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
            if (value != null) {
                nodeLabel.setImage(SharedImages.getImage(Boolean.valueOf((String) value) ? "icons/checked.gif" : "icons/unchecked.gif"));
            }
        }
    }
}

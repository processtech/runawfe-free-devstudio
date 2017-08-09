package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;

public class VariablesUsageEditorPage extends EditorPartBase<Variable> {

    private TableViewer tableViewer;
    private List<FormNode> formNodes = new ArrayList<>();
    private Map<String, List<FormNode>> variableUsage = new HashMap<>();
    
    public VariablesUsageEditorPage(ProcessEditorBase editor) {
        super(editor);
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = createSashForm(parent, SWT.VERTICAL, "DesignerVariableEditorPage.label.variablesUsage");

        Composite allVariablesComposite = createSection(sashForm, "DesignerVariableEditorPage.label.variables_usage");

        tableViewer = createMainViewer(allVariablesComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.setLabelProvider(new VariableLabelProvider());
        List<TableColumnDescription> columns = new ArrayList<>();
        columns.add(new TableColumnDescription("property.name", 300, SWT.LEFT));
        List<FormNode> allFormNodes = editor.getDefinition().getChildren(FormNode.class);
        for (FormNode formNode : allFormNodes) {
            if (true || formNode.hasForm()) {
                TableColumnDescription tcd = new TableColumnDescription(formNode.getId(), 50, SWT.CENTER, false);
                tcd.setToolTipText(formNode.getName());
                columns.add(tcd);
                formNodes.add(formNode);
            }
        }
        
        createTable(tableViewer, null, columns.toArray(new TableColumnDescription[0]));
        updateViewer();
    }

    @Override
    public void dispose() {
        for (Variable variable : getDefinition().getVariables(false, false)) {
            variable.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    private void updateViewer() {
        List<Variable> variables = getDefinition().getVariables(true, true);
        Collections.sort(variables);
        for (Variable variable : variables) {
            variableUsage.put(variable.getName(), ParContentProvider.getFormsWhereVariableUsed(editor.getDefinition().getFile(), editor.getDefinition(), variable.getName()));
        }
        tableViewer.setInput(variables);
    }

    private class VariableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            Variable variable = (Variable) element;
            if (index == 0) {
                return variable.getName();
            } else {
                FormNode fn = formNodes.get(index - 1);
                if (variableUsage.get(variable.getName()).contains(fn)) {
                    return Math.random() > .33 ? "R" : "R/W";
                } else {
                    return "";
                }
            }
        }

        @Override
        public String getText(Object element) {
            return getColumnText(element, 0);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

}

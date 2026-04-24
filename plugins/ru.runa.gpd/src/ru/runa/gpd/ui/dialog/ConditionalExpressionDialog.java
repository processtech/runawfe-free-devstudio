package ru.runa.gpd.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.extension.businessRule.BusinessRuleEditorDialog;
import ru.runa.gpd.extension.businessRule.BusinessRuleModel;
import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.ui.control.IntervalControl;

public class ConditionalExpressionDialog extends BusinessRuleEditorDialog {

    private final ConditionalEventModel conditionalEventModel;

    public ConditionalExpressionDialog(CatchEventNode node) {
        this(node.getProcessDefinition(), ConditionalEventModel.fromXml(node.getDelegationConfiguration()));
    }

    public ConditionalExpressionDialog(ProcessDefinition definition, String configuration) {
        this(definition, ConditionalEventModel.fromXml(configuration));
    }

    public ConditionalExpressionDialog(ProcessDefinition definition, ConditionalEventModel model) {
        super(definition, (model != null && model.getExpression() != null) ? model.getExpression() : "");
        this.conditionalEventModel = model;
    }

    @Override
    protected ExpressionLine createExpressionLine(int index, BusinessRuleModel model) {
        ExpressionLine expressionLine = new ConditionalExpressionLine(index, model);
        expressionLines.add(index, expressionLine);
        return expressionLine;
    }

    @Override
    protected void createBottomComposite() {
        Composite bottomComposite = new Composite(constructor, SWT.NONE);
        bottomComposite.setLayout(new GridLayout(2, false));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 5;
        bottomComposite.setLayoutData(data);

        defaultButton = new Button(bottomComposite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.exclude = true;
        defaultButton.setLayoutData(gridData);
        defaultButton.setVisible(false);
        defaultButton.setText("false");

        new IntervalControl(
                bottomComposite,
                conditionalEventModel.getInterval(),
                conditionalEventModel::setInterval,
                new GridData(SWT.FILL, SWT.CENTER, true, false)
        );
    }

    protected class ConditionalExpressionLine extends ExpressionLine {

        public ConditionalExpressionLine(int index, BusinessRuleModel model) {
            super(index, model);
        }

        @Override
        protected ExpressionLine newExpressionLine(int index, BusinessRuleModel model) {
            return new ConditionalExpressionLine(index, model);
        }

        @Override
        protected Button createFunctionButton() {
            Button button = new Button(this, SWT.NONE);
            GridData gridData = new GridData();
            gridData.exclude = true;
            button.setLayoutData(gridData);
            button.setVisible(false);
            button.setText("true");
            return button;
        }
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        conditionalEventModel.setExpression(getResult());
        result = conditionalEventModel.toXml();
    }
}

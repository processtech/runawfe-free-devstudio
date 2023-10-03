package ru.runa.gpd.office.store.externalstorage.predicate;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.businessRule.BracketPaintListener;
import ru.runa.gpd.extension.businessRule.LogicComposite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;

public class PredicateComposite extends Composite {
    private final StorageConstraintsModel constraintsModel;
    private final VariableProvider variableProvider;
    private final VariableUserType variableUserType;

    private ErrorHeaderComposite constructorHeader;

    private List<LogicComposite> logicComposites = new ArrayList<LogicComposite>();
    private Composite expressionsComposite;
    private int bracketsCount = 0;

    private final PredicateTree predicateTree = new PredicateTree();

    public PredicateComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, String variableTypeName,
            VariableProvider variableProvider) {
        super(parent, style);
        this.constraintsModel = constraintsModel;
        this.variableUserType = variableProvider.getUserType(variableTypeName);
        this.variableProvider = variableProvider;

        setLayout(new GridLayout(1, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        constructorHeader = new ErrorHeaderComposite(this);

        ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 5;
        scrolledComposite.setLayoutData(data);

        expressionsComposite = new Composite(scrolledComposite, SWT.NONE);
        expressionsComposite.setLayout(new GridLayout(1, true));
        expressionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setContent(expressionsComposite);
    }

    public void build() {
        if (!Strings.isNullOrEmpty(constraintsModel.getQueryString())) {
            final PredicateParser predicateParser = new PredicateParser(constraintsModel.getQueryString(), variableUserType, variableProvider);
            predicateTree.setHead(predicateParser.parse());
            if (!predicateTree.isEmpty()) {
                constructPredicateView();
            }
        }
    }

    public <X, Y> void addPredicate() {
        final VariablePredicate variablePredicate = new VariablePredicate();
        final int index = predicateTree.add(variablePredicate, expression -> buildCompoundTypeCombo(
                new OnConstructedPredicateDelegate<Object, VariablePredicate>(expression, this::onPredicateConstructed)));
        buildVariablePredicate(new OnConstructedPredicateDelegate<Variable, Variable>(variablePredicate, this::onPredicateConstructed), index);

        ((ScrolledComposite) expressionsComposite.getParent()).setMinSize(expressionsComposite.computeSize(SWT.MIN, SWT.DEFAULT));
    }

    private <X, Y> void constructPredicateView() {
        int currentVariablePredicateIndex = 0;
        for (ConstraintsPredicate<?, ?> predicate : predicateTree) {
            if (predicate instanceof VariablePredicate) {
                buildVariablePredicate(
                        new OnConstructedPredicateDelegate<Variable, Variable>((VariablePredicate) predicate, this::onPredicateConstructed),
                        currentVariablePredicateIndex++);
            } else {
                buildCompoundTypeCombo(new OnConstructedPredicateDelegate<X, Y>((ExpressionPredicate<?>) predicate, this::onPredicateConstructed));
            }
        }
    }

    private void buildCompoundTypeCombo(ConstraintsPredicate<?, ?> predicate) {
        LogicComposite lc = logicComposites.get(logicComposites.size() - 1);
        lc.setVisible(true);

        final Combo compoundTypeCombo = lc.getLogicBox();
        compoundTypeCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            final String text = compoundTypeCombo.getText();
            if (!Strings.isNullOrEmpty(text)) {
                predicate.setType(PredicateOperationType.byCode(text).orElse(null));
            }
        }));
        if (predicate.getType() != null) {
            compoundTypeCombo.setText(predicate.getType().code);
        }
    }

    private void buildVariablePredicate(OnConstructedPredicateDelegate<Variable, Variable> predicate, int index) {
        Composite expression = new Composite(expressionsComposite, SWT.NONE);
        GridLayout expressionLayout = new GridLayout(5, false);
        expressionLayout.verticalSpacing = 0;
        expression.setLayout(expressionLayout);
        expression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Combo subjectCombo = new Combo(expression, SWT.READ_ONLY);
        subjectCombo.setToolTipText(Messages.getString("label.DBTableField"));
        final Combo predicateOperationTypeCombo = new Combo(expression, SWT.READ_ONLY);
        predicateOperationTypeCombo.setToolTipText(Messages.getString("label.ComparisonOperation"));
        final Combo compareWithCombo = new Combo(expression, SWT.READ_ONLY);
        compareWithCombo.setToolTipText(Messages.getString("label.variable"));

        LogicComposite logicComposite = new LogicComposite(expression, logicComposites);
        logicComposite.getLogicBox().setToolTipText(Messages.getString("label.LogicOperation"));
        logicComposites.add(logicComposite);
        logicComposite.setVisible(false);
        logicComposite.setBrackets(predicate.getBrackets());
        expression.addPaintListener(new BracketPaintListener(logicComposites, expression, logicComposite));

        logicComposite.updateVerticalMargin(index);
        bracketsCount += logicComposite.getBrackets()[0];
        ((GridLayout) expression.getLayout()).marginLeft = bracketsCount * LogicComposite.MARGIN_LEFT_STEP;
        bracketsCount -= logicComposite.getBrackets()[1];

        logicComposite.getCloseButton().addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            predicate.setBrackets(logicComposite.getBrackets());
        }));
        logicComposite.getOpenButton().addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            predicate.setBrackets(logicComposite.getBrackets());
        }));

        predicate.applicableOperationTypeNames().forEach(predicateOperationTypeCombo::add);
        predicateOperationTypeCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            final String text = predicateOperationTypeCombo.getText();
            if (!Strings.isNullOrEmpty(text)) {
                predicate.setType(PredicateOperationType.byCode(text).orElse(null));
            }
        }));
        if (predicate.getType() != null) {
            predicateOperationTypeCombo.setText(predicate.getType().code);
        }

        variableUserType.getAttributes().stream().map(Variable::getName).forEach(subjectCombo::add);
        subjectCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
            final String text = subjectCombo.getText();
            if (Strings.isNullOrEmpty(text)) {
                return;
            }
            final Optional<Variable> variable = variableUserType.getAttributes().stream().filter(var -> var.getName().equals(text)).findAny();
            variable.ifPresent(var -> {
                predicate.setLeft(var);
                compareWithCombo.removeAll();
                variableProvider.variableNamesAccordingToType(var.getFormatClassName()).forEach(compareWithCombo::add);
            });
        }));
        if (predicate.getLeft() != null) {
            subjectCombo.setText(predicate.getLeft().getName());
            variableProvider.variableNamesAccordingToType(predicate.getLeft().getFormatClassName()).forEach(compareWithCombo::add);
        }

        compareWithCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
            final String text = compareWithCombo.getText();
            if (!Strings.isNullOrEmpty(text)) {
                variableProvider.variableByName(text).ifPresent(predicate::setRight);
            }
            getParent().layout(true, true);
        }));
        if (predicate.getRight() != null) {
            compareWithCombo.setText(predicate.getRight().getName());
        }

        SwtUtils.createLink(expression, "[X]", new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                onDeletePredicate(index);
            }
        });
    }

    private void onPredicateConstructed() {
        boolean predicateConstructed = true;
        if (!predicateTree.isEmpty()) {
            for (ConstraintsPredicate<?, ?> predicate : predicateTree) {
                if (!predicate.isComplete()) {
                    setErrorLabelText(Localization.getString("GroovyEditor.fillAll"));
                    predicateConstructed = false;
                    break;
                }
            }
        }
        if (predicateConstructed) {
            clearErrorLabelText();
            constraintsModel.setQueryString(predicateTree.head() != null ? predicateTree.head().toString().trim() : "");
        }
    }

    private void onDeletePredicate(int index) {
        logicComposites.get(index).updateBeforeDeletion();
        logicComposites.remove(index);

        predicateTree.removeVariablePredicateBy(index);
        int currentVariablePredicateIndex = 0;
        if (!predicateTree.isEmpty()) {
            for (ConstraintsPredicate<?, ?> predicate : predicateTree) {
                if (predicate instanceof VariablePredicate) {
                    predicate.setBrackets(logicComposites.get(currentVariablePredicateIndex).getBrackets());
                    currentVariablePredicateIndex++;
                }
            }
        }

        onPredicateConstructed();
        logicComposites.clear();
        for (Control c : expressionsComposite.getChildren()) {
            c.dispose();
        }
        build();
        logicComposites.get(0).updateAfterDeletion();
        getParent().layout(true, true);
    }

    protected class ErrorHeaderComposite extends Composite {
        private final Label errorLabel;

        public ErrorHeaderComposite(Composite parent) {
            super(parent, SWT.NONE);
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            setLayout(new GridLayout(6, false));
            errorLabel = new Label(this, SWT.NONE);
            errorLabel.setForeground(new org.eclipse.swt.graphics.Color(Display.getCurrent(), 255, 0, 0));
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        public void setErrorText(String text) {
            errorLabel.setText(text);
        }

        public void clearErrorText() {
            setErrorText("");
        }
    }

    protected void setErrorLabelText(String text) {
        constructorHeader.setErrorText(text);
    }

    protected void clearErrorLabelText() {
        constructorHeader.clearErrorText();
    }
}

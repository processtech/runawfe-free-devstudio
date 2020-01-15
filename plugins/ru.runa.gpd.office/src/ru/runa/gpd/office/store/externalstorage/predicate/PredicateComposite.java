package ru.runa.gpd.office.store.externalstorage.predicate;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

public class PredicateComposite extends Composite {
    private final StorageConstraintsModel constraintsModel;
    private final String variableTypeName;
    private final VariableProvider variableProvider;

    private final Group group;
    private final List<Label> labels = new ArrayList<>();

    private ConstraintsPredicate<?, ?> constraintsPredicate;

    public PredicateComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, String variableTypeName,
            VariableProvider variableProvider) {
        super(parent, style);
        this.constraintsModel = constraintsModel;
        this.variableTypeName = variableTypeName;
        this.variableProvider = variableProvider;

        setLayout(new GridLayout(5, false));
        final GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 5;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));
        group = new Group(this, SWT.None);
        group.setLayout(new GridLayout(5, false));
    }

    public void build() {
        if (!Strings.isNullOrEmpty(constraintsModel.getQueryString())) {
            final PredicateParser predicateParser = new PredicateParser(constraintsModel.getQueryString(),
                    variableProvider.getUserType(variableTypeName), variableProvider);
            constraintsPredicate = predicateParser.parse();
            if (constraintsPredicate != null) {
                buildLabels();
                constructPredicateViewRecursive(constraintsPredicate);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addPredicate() {
        final VariablePredicate variablePredicate = new VariablePredicate();
        if (constraintsPredicate != null) {
            final ExpressionPredicate<?> predicate = new ExpressionPredicate(constraintsPredicate, PredicateOperationType.AND, variablePredicate);
            buildCompoundTypeCombo(new OnConstructedPredicateDelegate<Object, VariablePredicate>(predicate, this::onPredicateConstructed));

            constraintsPredicate.setParent(predicate);
            variablePredicate.setParent(predicate);
            constraintsPredicate = predicate;
        } else {
            constraintsPredicate = variablePredicate;
        }
        buildLabels();
        buildVariablePredicate(new OnConstructedPredicateDelegate<Variable, Variable>(variablePredicate, this::onPredicateConstructed));
    }

    public <X, Y> void constructPredicateViewRecursive(ConstraintsPredicate<X, Y> constraintsPredicate) {
        if (constraintsPredicate instanceof VariablePredicate) {
            final VariablePredicate predicate = (VariablePredicate) constraintsPredicate;
            buildVariablePredicate(new OnConstructedPredicateDelegate<Variable, Variable>(predicate, this::onPredicateConstructed));
        } else {
            final ExpressionPredicate<?> predicate = (ExpressionPredicate<?>) constraintsPredicate;
            constructPredicateViewRecursive((ConstraintsPredicate<?, ?>) constraintsPredicate.getLeft());
            buildCompoundTypeCombo(new OnConstructedPredicateDelegate<X, Y>(predicate, this::onPredicateConstructed));
            constructPredicateViewRecursive((ConstraintsPredicate<?, ?>) constraintsPredicate.getRight());
        }
    }

    private void buildCompoundTypeCombo(ConstraintsPredicate<?, ?> predicate) {
        final Combo compoundTypeCombo = new Combo(group, SWT.READ_ONLY);
        predicate.applicableOperationTypeNames().forEach(compoundTypeCombo::add);
        compoundTypeCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            final String text = compoundTypeCombo.getText();
            if (Strings.isNullOrEmpty(text)) {
                return;
            }
            predicate.setType(PredicateOperationType.byCode(text).orElse(null));
        }));
        if (predicate.getType() != null) {
            compoundTypeCombo.setText(predicate.getType().code);
        }
    }

    private void buildVariablePredicate(OnConstructedPredicateDelegate<Variable, Variable> predicate) {
        final Combo subjectCombo = new Combo(group, SWT.READ_ONLY);
        final Combo predicateOperationTypeCombo = new Combo(group, SWT.READ_ONLY);
        final Combo compareWithCombo = new Combo(group, SWT.READ_ONLY);

        predicate.applicableOperationTypeNames().forEach(predicateOperationTypeCombo::add);
        predicateOperationTypeCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
            final String text = predicateOperationTypeCombo.getText();
            if (Strings.isNullOrEmpty(text)) {
                return;
            }
            predicate.setType(PredicateOperationType.byCode(text).orElse(null));
        }));
        if (predicate.getType() != null) {
            predicateOperationTypeCombo.setText(predicate.getType().code);
        }

        final VariableUserType variableUserType = variableProvider.getUserType(variableTypeName);
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
            if (Strings.isNullOrEmpty(text)) {
                return;
            }
            final Optional<Variable> variable = variableProvider.variableByName(text);
            variable.ifPresent(var -> {
                predicate.setRight(var);
            });
        }));
        if (predicate.getRight() != null) {
            compareWithCombo.setText(predicate.getRight().getName());
        }

        SWTUtils.createLink(group, "[X]", new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                onDeletePredicate((VariablePredicate) predicate.getDelegate());
            }
        });
    }

    private void onPredicateConstructed(String predicate) {
        constraintsModel.setQueryString(constraintsPredicate != null ? constraintsPredicate.toString().trim() : "");
    }

    @SuppressWarnings("unchecked")
    private void onDeletePredicate(VariablePredicate predicate) {
        if (Objects.equals(constraintsPredicate, predicate)) {
            constraintsPredicate = null;
        } else if (predicate.getParent().getParent() != null) {
            final ConstraintsPredicate<?, ?> otherChild = !Objects.equals(predicate, predicate.getParent().getLeft())
                    ? (ConstraintsPredicate<?, ?>) predicate.getParent().getLeft()
                    : (ConstraintsPredicate<?, ?>) predicate.getParent().getRight();
            otherChild.setParent(predicate.getParent().getParent());
            ((ExpressionPredicate<ConstraintsPredicate<?, ?>>) predicate.getParent().getParent()).setLeft(otherChild);
            predicate.setParent(null);
        } else if (predicate.getParent().getParent() == null) {
            final ConstraintsPredicate<?, ?> otherChild = !Objects.equals(predicate, predicate.getParent().getLeft())
                    ? (ConstraintsPredicate<?, ?>) predicate.getParent().getLeft()
                    : (ConstraintsPredicate<?, ?>) predicate.getParent().getRight();
            predicate.setParent(null);
            otherChild.setParent(null);
            constraintsPredicate = otherChild;
        }

        onPredicateConstructed(null);

        labels.clear();
        for (Control c : group.getChildren()) {
            c.dispose();
        }
        build();
        getParent().layout(true, true);
    }

    private void buildLabels() {
        if (!labels.isEmpty()) {
            if (constraintsPredicate instanceof ExpressionPredicate<?>) {
                labels.get(4).setText(Messages.getString("label.LogicOperation"));
            }
            return;
        } else {
            labels.add(SWTUtils.createLabel(group, Messages.getString("label.DBTableField")));
            labels.add(SWTUtils.createLabel(group, Messages.getString("label.ComparisonOperation")));
            labels.add(SWTUtils.createLabel(group, Messages.getString("label.variable")));
            labels.add(SWTUtils.createLabel(group, ""));
            labels.add(SWTUtils.createLabel(group,
                    (constraintsPredicate instanceof ExpressionPredicate<?>) ? Messages.getString("label.LogicOperation") : ""));
        }
        getParent().layout(true, true);
    }
}

package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import com.google.common.base.Strings;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.office.store.externalstorage.predicate.PredicateComposite;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

public class SelectConstraintsComposite extends AbstractOperatingVariableComboBasedConstraintsCompositeBuilder {
    private final Consumer<String> resultVariableNameConsumer;
    private PredicateComposite predicateComposite;

    public SelectConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableContainer variableContainer,
            String variableTypeName, Consumer<String> resultVariableNameConsumer) {
        super(parent, style, constraintsModel, variableContainer, variableTypeName);
        this.resultVariableNameConsumer = resultVariableNameConsumer;
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        produceResultVariableName(null);
    }

    @Override
    public void build() {
        super.build();

        if (!Strings.isNullOrEmpty(variableTypeName)) {
            SWTUtils.createLink(getParent(), Messages.getString("label.AddPredicate"), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    if (predicateComposite == null) {
                        return;
                    }
                    predicateComposite.addPredicate();
                    getParent().layout(true, true);
                }
            }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

            final ProcessDefinition processDefinition = variableContainer.getVariables(false, true).stream()
                    .map(variable -> variable.getProcessDefinition()).findAny()
                    .orElseThrow(() -> new IllegalStateException("process definition unavailable"));
            predicateComposite = new PredicateComposite(getParent(), getStyle(), constraintsModel, variableTypeName, processDefinition);
            predicateComposite.build();
        }

        getParent().layout(true, true);
    }

    @Override
    protected void onWidgetSelected(String text) {
        super.onWidgetSelected(text);
        produceResultVariableName(text);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate() {
        return variable -> variable.getFormatComponentClassNames()[0].equals(variableTypeName);
    }

    @Override
    protected String getComboTitle() {
        return Messages.getString("label.SelectResultVariable");
    }

    @Override
    protected String[] getTypeNameFilters() {
        return new String[] { List.class.getName() };
    }

    private void produceResultVariableName(String variableName) {
        if (resultVariableNameConsumer != null) {
            resultVariableNameConsumer.accept(variableName);
        }
    }

}

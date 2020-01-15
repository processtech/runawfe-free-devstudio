package ru.runa.gpd.office.store.externalstorage;

import com.google.common.base.Strings;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.gpd.office.store.externalstorage.predicate.PredicateComposite;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

public class PredicateCompositeDelegateBuilder implements ConstraintsCompositeBuilder {
    private final Composite parent;
    private final int style;
    private final StorageConstraintsModel constraintsModel;
    private final VariableProvider variableProvider;
    private final ConstraintsCompositeBuilder delegate;

    private PredicateComposite predicateComposite;
    private String variableTypeName;

    public PredicateCompositeDelegateBuilder(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            String variableTypeName, ConstraintsCompositeBuilder delegate) {
        this.parent = parent;
        this.style = style;
        this.constraintsModel = constraintsModel;
        this.variableProvider = variableProvider;
        this.variableTypeName = variableTypeName;
        this.delegate = delegate;
    }

    @Override
    public void build() {
        delegate.build();

        if (!Strings.isNullOrEmpty(variableTypeName)) {
            SWTUtils.createLink(parent, Messages.getString("label.AddPredicate"), new LoggingHyperlinkAdapter() {
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    if (predicateComposite == null) {
                        return;
                    }
                    predicateComposite.addPredicate();
                    parent.layout(true, true);
                }
            }).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

            predicateComposite = new PredicateComposite(parent, style, constraintsModel, variableTypeName, variableProvider);
            predicateComposite.build();
        }

        parent.layout(true, true);
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        delegate.onChangeVariableTypeName(variableTypeName);
    }

    @Override
    public void clearConstraints() {
        delegate.clearConstraints();
    }

}

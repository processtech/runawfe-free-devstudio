package ru.runa.gpd.formeditor.ftl.ui.dialog.projection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

public class ProjectionDialog extends XmlBasedConstructorProvider<ProjectionDataModel> {
    private final VariableUserType userType;

    public ProjectionDialog(VariableUserType userType) {
        this.userType = userType;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("ProjectionDialog.title");
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, ProjectionDataModel model) {
        return new ProjectionView(parent, delegable, model).build();
    }

    @Override
    protected ProjectionDataModel createDefault() {
        return ProjectionDataModel.by(userType);
    }

    @Override
    protected ProjectionDataModel fromXml(String xml) throws Exception {
        return ProjectionDataModel.fromXml(xml).orElseGet(() -> createDefault());
    }

    private class ProjectionView extends ConstructorComposite {

        public ProjectionView(Composite parent, Delegable delegable, ProjectionDataModel model) {
            super(parent, delegable, model);
            setLayout(new GridLayout(3, false));
        }

        public Composite build() {
            buildFromModel();
            return this;
        }

        @Override
        protected void buildFromModel() {
            for (Control control : getChildren()) {
                control.dispose();
            }

            SWTUtils.createLabel(this, Messages.getString("ProjectionDialog.label.attributes"));
            SWTUtils.createLabel(this, "");
            SWTUtils.createLabel(this, Messages.getString("ProjectionDialog.label.sort"));

            for (Projection projection : model.getProjections()) {
                buildProjectionView(projection);
            }

            ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
            updateComponents();
        }

        private void buildProjectionView(Projection projection) {
            SWTUtils.createLabel(this, projection.getName());

            final Button button = SWTUtils.createButton(this, projection.getVisibility().getMessage(), null);
            button.addSelectionListener(LoggingSelectionAdapter.widgetSelectedAdapter(e -> {
                projection.setVisibility(projection.getVisibility() == Visibility.VISIBLE ? Visibility.INVISIBLE : Visibility.VISIBLE);
                button.setText(projection.getVisibility().getMessage());
                updateComponents();
            }));

            final Combo combo = new Combo(this, SWT.READ_ONLY);
            Sort.messages().forEach(combo::add);
            combo.setText(projection.getSort().getMessage());

            combo.addSelectionListener(
                    LoggingSelectionAdapter.widgetSelectedAdapter(e -> projection.setSort(Sort.by(combo.getText()).orElse(Sort.NONE))));
        }
        
        private void updateComponents() {
            this.layout(true, true);
            this.redraw();
        }

    }

}

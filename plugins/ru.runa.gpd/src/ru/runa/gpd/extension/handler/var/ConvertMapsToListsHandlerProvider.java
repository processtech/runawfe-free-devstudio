package ru.runa.gpd.extension.handler.var;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.wfe.extension.handler.var.ConvertMapToListOperation;
import ru.runa.wfe.extension.handler.var.ConvertMapsToListsConfig;
import ru.runa.wfe.extension.handler.var.ConvertMapsToListsConfig.Sorting;

public class ConvertMapsToListsHandlerProvider extends XmlBasedConstructorProvider<ConvertMapsToListsConfig> {
    @Override
    protected ConvertMapsToListsConfig createDefault() {
        return new ConvertMapsToListsConfig();
    }

    @Override
    protected ConvertMapsToListsConfig fromXml(String xml) throws Exception {
        return ConvertMapsToListsConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorComposite(Composite parent, Delegable delegable, ConvertMapsToListsConfig model) {
        return new ConstructorView(parent, delegable, model);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("ConvertMapsToListsConfig.title");
    }

    private class ConstructorView extends ConstructorComposite {

        public ConstructorView(Composite parent, Delegable delegable, ConvertMapsToListsConfig model) {
            super(parent, delegable, model);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        protected void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                addRootSection();
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addRootSection() {
            Composite paramsComposite = createParametersComposite(this);
            int index = 0;
            for (ConvertMapToListOperation operation : model.getOperations()) {
                addOperationSection(paramsComposite, operation, index);
                index++;
            }
            createStrokeComposite(this, Localization.getString("ConvertMapsToListsConfig.sorting"), null);
            {
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Localization.getString("none"));
                combo.add(Localization.getString("ConvertMapsToListsConfig.sorting." + Sorting.KEYS));
                for (ConvertMapToListOperation operation : model.getOperations()) {
                    combo.add(Localization.getString("ConvertMapsToListsConfig.sorting." + Sorting.VALUES) + " " + operation.getMapVariableName());
                }
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                String sortBy = model.getSorting().getSortBy();
                int selectedIndex;
                if (Sorting.NONE.equals(sortBy)) {
                    selectedIndex = 0;
                } else if (Sorting.KEYS.equals(sortBy)) {
                    selectedIndex = 1;
                } else {
                    selectedIndex = Integer.parseInt(sortBy.substring(Sorting.VALUES.length())) + 2;
                }
                combo.select(selectedIndex);
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        int selectedIndex = combo.getSelectionIndex();
                        String sortBy;
                        if (selectedIndex == 0) {
                            sortBy = Sorting.NONE;
                        } else if (selectedIndex == 1) {
                            sortBy = Sorting.KEYS;
                        } else {
                            sortBy = Sorting.VALUES + (selectedIndex - 2);
                        }
                        model.getSorting().setSortBy(sortBy);
                    }
                });
            }
            {
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Localization.getString("sorting." + Sorting.MODE_ASC));
                combo.add(Localization.getString("sorting." + Sorting.MODE_DESC));
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.select(Sorting.MODE_ASC.equals(model.getSorting().getSortMode()) ? 0 : 1);
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String mode = combo.getSelectionIndex() == 0 ? Sorting.MODE_ASC : Sorting.MODE_DESC;
                        model.getSorting().setSortMode(mode);
                    }
                });
            }
        }

        private void createStrokeComposite(Composite parent, String label, LoggingHyperlinkAdapter hyperlinkAdapter) {
            Composite strokeComposite = new Composite(parent, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(hyperlinkAdapter != null ? 4 : 3, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(label);
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (hyperlinkAdapter != null) {
                SwtUtils.createLink(strokeComposite, Localization.getString("button.add"), hyperlinkAdapter);
            }
        }

        private Composite createParametersComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            composite.setLayoutData(data);
            createStrokeComposite(composite, Localization.getString("ConvertMapsToListsConfig.label.operations"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addOperation();
                }
            });
            return composite;
        }

        private void addOperationSection(Composite parent, final ConvertMapToListOperation operation, final int index) {
            {
                final Combo combo = new Combo(parent, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(false, Map.class.getName())) {
                    combo.add(variableName);
                }
                combo.setText(operation.getMapVariableName());
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        operation.setMapVariableName(combo.getText());
                    }
                });
            }
            {
                final Combo combo = new Combo(parent, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(false, List.class.getName())) {
                    combo.add(variableName);
                }
                combo.setText(operation.getListVariableName());
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        operation.setListVariableName(combo.getText());
                    }
                });
            }
            SwtUtils.createLink(parent, "[X]", new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.deleteOperation(index);
                }
            });
        }
    }
}

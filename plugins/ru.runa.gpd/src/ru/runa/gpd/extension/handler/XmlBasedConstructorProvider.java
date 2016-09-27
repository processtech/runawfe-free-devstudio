package ru.runa.gpd.extension.handler;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class XmlBasedConstructorProvider<T extends Observable> extends DelegableProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        XmlBasedConstructorDialog dialog = new XmlBasedConstructorDialog(delegable);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (String variableName : delegable.getVariableNames(true)) {
            if (configuration.contains("\"" + variableName + "\"")) {
                result.add(variableName);
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        String oldString = Pattern.quote("\"" + currentVariable.getName() + "\"");
        String newString = Matcher.quoteReplacement("\"" + previewVariable.getName() + "\"");
        return delegable.getDelegationConfiguration().replaceAll(oldString, newString);
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) throws Exception {
        String configuration = delegable.getDelegationConfiguration();
        T model = fromXml(configuration);
        return validateModel(delegable, model, errors);
    }

    protected boolean validateModel(Delegable delegable, T model, List<ValidationError> errors) {
        return true;
    }

    protected abstract String getTitle();

    protected abstract Composite createConstructorComposite(Composite parent, Delegable delegable, T model);

    protected abstract T createDefault();

    protected abstract T fromXml(String xml) throws Exception;

    protected int getSelectedTabIndex(Delegable delegable, T model) {
        return 0;
    }

    public abstract class ConstructorComposite extends Composite implements Observer {
        protected T model;
        protected final Delegable delegable;

        public ConstructorComposite(Composite parent, Delegable delegable, T model) {
            super(parent, SWT.NONE);
            this.delegable = delegable;
            this.model = model;
        }

        @Override
        public final void update(Observable model, Object o) {
            this.model = (T) model;
            buildFromModel();
        }

        protected abstract void buildFromModel();

    }

    private class XmlBasedConstructorDialog extends Dialog {
        protected T model;
        private TabFolder tabFolder;
        private XmlContentView xmlContentView;
        private Composite constructorView;
        private final String initialValue;
        private String result;
        protected final Delegable delegable;

        public XmlBasedConstructorDialog(Delegable delegable) {
            super(Display.getCurrent().getActiveShell());
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.delegable = delegable;
            this.initialValue = delegable.getDelegationConfiguration();
        }

        @Override
        protected Point getInitialSize() {
            return new Point(600, 400);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText(getTitle());
            tabFolder = new TabFolder(parent, SWT.BORDER);
            tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            Composite composite = new Composite(tabFolder, SWT.NONE);
            composite.setLayout(new GridLayout());
            TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("SQLActionHandlerConfig.title.configuration"));
            tabItem1.setControl(composite);
            ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            try {
                if (initialValue.trim().length() != 0) {
                    model = fromXml(initialValue);
                } else {
                    model = createDefault();
                }
            } catch (Exception ex) {
                PluginLogger.logError(Localization.getString("config.error.parse"), ex);
                model = createDefault();
            }
            constructorView = createConstructorComposite(scrolledComposite, delegable, model);
            constructorView.setLayoutData(new GridData(GridData.FILL_BOTH));
            if (constructorView instanceof Observer) {
                model.addObserver((Observer) constructorView);
            }
            scrolledComposite.setContent(constructorView);
            xmlContentView = new XmlContentView(tabFolder, SWT.NONE);
            xmlContentView.setLayoutData(new GridData(GridData.FILL_BOTH));
            xmlContentView.setValue(initialValue);
            TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
            tabItem2.setText(" XML ");
            tabItem2.setControl(xmlContentView);
            tabFolder.setSelection(getSelectedTabIndex(delegable, model));
            tabFolder.addSelectionListener(new TabSelectionHandler());
            return tabFolder;
        }

        @Override
        protected void okPressed() {
            if (tabFolder.getSelectionIndex() == 0) {
                populateToSourceView();
            }
            if (xmlContentView.validate()) {
                this.result = xmlContentView.getValue();
                super.okPressed();
            }
        }

        private void populateToConstructorView() {
            try {
                String xml = xmlContentView.getValue();
                if (constructorView instanceof Observer && model != null) {
                    Observer observer = (Observer) constructorView;
                    model.deleteObserver(observer);
                }
                model = fromXml(xml);
                if (constructorView instanceof Observer) {
                    Observer observer = (Observer) constructorView;
                    model.addObserver(observer);
                    // constructorView.buildFromModel();
                    observer.update(model, null);
                    constructorView.layout(true, true);
                }
            } catch (Exception ex) {
                PluginLogger.logError("Unable to parse model from XML", ex);
            }
        }

        private void populateToSourceView() {
            xmlContentView.setValue(model.toString());
        }

        public String getResult() {
            return result;
        }

        private class TabSelectionHandler extends SelectionAdapter {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (tabFolder.getSelectionIndex() == 0) {
                    if (xmlContentView.validate()) {
                        populateToConstructorView();
                    } else {
                        // return to source view due to invalid xml
                        tabFolder.setSelection(1);
                    }
                } else {
                    populateToSourceView();
                }
            }
        }
    }

    public static class XmlContentView extends Composite {
        private final StyledText styledText;
        private final Label errorLabel;

        public XmlContentView(Composite parent, int style) {
            super(parent, style);
            setLayout(new GridLayout());
            errorLabel = new Label(this, SWT.NONE);
            errorLabel.setForeground(ColorConstants.red);
            errorLabel.setText("");
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            styledText = new StyledText(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
            XmlHighlightTextStyling lineStyleListener = new XmlHighlightTextStyling();
            styledText.addLineStyleListener(lineStyleListener);
            styledText.setLineSpacing(2);
        }

        private void setErrorLabelText(String text) {
            errorLabel.setText(text);
            errorLabel.pack(true);
        }

        public boolean validate() {
            try {
                String xml = styledText.getText();
                XmlUtil.parseWithoutValidation(xml);
                return true;
            } catch (Exception e) {
                setErrorLabelText(e.getMessage());
            }
            return false;
        }

        public void setValue(String value) {
            styledText.setText(value);
        }

        public String getValue() {
            return styledText.getText();
        }
    }
}

package ru.runa.gpd.extension.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class SendEmailActionHandlerProvider extends DelegableProvider {
    @Override
    public String showConfigurationDialog(IDelegable iDelegable) {
        final EmailConfigWizardPage wizardPage = new EmailConfigWizardPage(bundle, iDelegable);
        final ConfigurationWizard wizard = new ConfigurationWizard(wizardPage);
        CompactWizardDialog wizardDialog = new CompactWizardDialog(wizard) {
            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                Button testButton = createButton(parent, 101, Localization.getString("EmailDialog.test.button"), false);
                testButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        final String cfg = wizardPage.generateCode();
                        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
                        monitorDialog.setCancelable(true);
                        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                            @Override
                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                try {
                                    monitor.beginTask(Localization.getString("EmailDialog.test.button"), 1);
                                    EmailConfig config = EmailConfigParser.parse(cfg);
                                    EmailUtils.sendMessage(config);
                                } catch (Exception e) {
                                    throw new InvocationTargetException(e);
                                } finally {
                                    monitor.done();
                                }
                            }
                        };
                        try {
                            monitorDialog.run(true, false, runnable);
                            setMessage(Localization.getString("EmailDialog.test.success"));
                        } catch (InvocationTargetException ex) {
                            PluginLogger.logError(Localization.getString("EmailDialog.test.error"), ex.getTargetException());
                        } catch (InterruptedException ex) {
                        }
                    }
                });
                Button copyButton = createButton(parent, 197, Localization.getString("button.copy"), false);
                copyButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Clipboard clipboard = new Clipboard(Display.getCurrent());
                        clipboard.setContents(new String[] { wizardPage.generateCode() }, new Transfer[] { TextTransfer.getInstance() });
                        clipboard.dispose();
                    }
                });
                super.createButtonsForButtonBar(parent);
            }

            @Override
            protected Point getInitialSize() {
                return new Point(800, 600);
            }
        };
        if (wizardDialog.open() == IDialogConstants.OK_ID) {
            return wizardPage.getResult();
        }
        return null;
    }

    @Override
    public List<String> getUsedVariableNames(IDelegable iDelegable) {
        String configuration = iDelegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (String variableName : iDelegable.getVariableNames(true)) {
            if (configuration.contains("\"" + variableName + "\"")) {
                result.add(variableName);
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(IDelegable iDelegable, Variable currentVariable, Variable previewVariable) {
        String oldString = Pattern.quote("\"" + currentVariable.getName() + "\"");
        String newString = Matcher.quoteReplacement("\"" + previewVariable.getName() + "\"");
        return iDelegable.getDelegationConfiguration().replaceAll(oldString, newString);
    }

    @Override
    public boolean validateValue(IDelegable iDelegable, List<ValidationError> errors) {
        String configuration = iDelegable.getDelegationConfiguration();
        if (configuration.trim().length() > 0) {
            EmailConfig config = EmailConfigParser.parse(configuration, false);
            GraphElement parent = ((GraphElement) iDelegable).getParent();
            if (config.isUseMessageFromTaskForm()) {
                if (parent instanceof TaskState) {
                    TaskState taskState = (TaskState) parent;
                    if (!taskState.hasForm()) {
                        errors.add(ValidationError.createLocalizedError((GraphElement) iDelegable, "delegable.email.taskform.empty"));
                    } else if (!"ftl".equals(taskState.getFormType())) {
                        errors.add(ValidationError.createLocalizedError((GraphElement) iDelegable, "delegable.email.taskform.notftl"));
                    }
                } else {
                    errors.add(ValidationError.createLocalizedError((GraphElement) iDelegable, "delegable.email.taskform.nocontext"));
                }
            }
        }
        return true;
    }

    public class ConfigurationWizard extends Wizard {
        private final EmailConfigWizardPage wizardPage;

        public ConfigurationWizard(EmailConfigWizardPage wizardPage) {
            this.wizardPage = wizardPage;
            setWindowTitle(Localization.getString("property.delegation.configuration"));
        }

        @Override
        public void addPages() {
            addPage(wizardPage);
        }

        @Override
        public boolean performFinish() {
            wizardPage.generateCode();
            return true;
        }
    }
}

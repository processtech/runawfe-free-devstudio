package ru.runa.gpd.quick.formeditor.ui;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import ru.runa.gpd.quick.Activator;
import ru.runa.gpd.quick.Messages;

public class PreviewFormWizard extends Wizard implements INewWizard {
    private final PreviewFormWizardPage page = new PreviewFormWizardPage();
    private final String formHtml;
    private final String styles;

    public PreviewFormWizard(String formHtml, String styles) {
        this.formHtml = formHtml;
        this.styles = styles;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public void addPages() {
        addPage(page);
    }

    private class PreviewFormWizardPage extends WizardPage {

        protected PreviewFormWizardPage() {
            super("PreviewFormWizardPage");
            setTitle(Messages.getString("PreviewFormWizardPage.page.title"));
        }

        @Override
        public void createControl(Composite parent) {
            Browser browser = new Browser(parent, SWT.NULL);
            browser.setLayoutData(new GridData(GridData.FILL_BOTH));
            browser.setSize(800, 600);
            try {
                String background = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("/icons/runawfeweb.jpg")).toString();
                String html = "<html>\n<head>\n<style>\n";
                if (styles != null) {
                    html += styles;
                }
                html += "</style>\n</head>\n<BODY style=\"background: url(" + background + ") top left no-repeat;\">\n";
                html += "<div class=\"taskform\" style=\"margin-left: 260px; margin-top: 120px; width: 100%;\">\n";
                html += formHtml;
                html += "</div>\n</body>\n</html>";
                browser.setText(html);
            } catch (Exception e) {
                throw new RuntimeException("Error loading background img", e);
            }
            setControl(browser);
        }

    }
}

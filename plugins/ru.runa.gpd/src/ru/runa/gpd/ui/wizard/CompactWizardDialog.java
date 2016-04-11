package ru.runa.gpd.ui.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Without progress bar area
 */
public class CompactWizardDialog extends WizardDialog {
    public CompactWizardDialog(Wizard wizard) {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        wizard.setNeedsProgressMonitor(false);        
    }

    @Override
    protected IProgressMonitor getProgressMonitor() {
        // To avoid 'Widget is disposed' at org.eclipse.jface.wizard.ProgressMonitorPart.done(ProgressMonitorPart.java:184)
        return new NullProgressMonitor();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        ((ProgressMonitorPart) super.getProgressMonitor()).dispose();
        return control;
    }
}

package ru.runa.gpd.aichat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class EarlyStartup implements IStartup {
    @Override
    public void earlyStartup() {
        Display.getDefault().asyncExec(() -> Activator.getDefault().initializePreferences());
    }
}

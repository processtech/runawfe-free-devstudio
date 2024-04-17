package ru.runa.gpd.ui.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class DefaultPropertySheetPage extends PropertySheetPage {

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // Disable context menu for empty default page
        getControl().setMenu(null);
    }

}

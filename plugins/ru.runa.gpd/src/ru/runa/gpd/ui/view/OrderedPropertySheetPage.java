package ru.runa.gpd.ui.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;

public class OrderedPropertySheetPage extends PropertySheetPage {

    public OrderedPropertySheetPage() {
        super();
        setSorter(new PropertySheetSorter() {
            @Override
            public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB) {
                return 1; // Do not changes the logical order of properties. Prevents default alphabetical sorting.
            }
        }
        );
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // 3274#note-8
        getControl().setMenu(null);
    }
}

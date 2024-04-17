package ru.runa.gpd.ui.view;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;

public class CustomPropertySheet extends PropertySheet {

    @Override
    protected IPage createDefaultPage(PageBook book) {
        IPageBookViewPage page = (IPageBookViewPage) Adapters.adapt(this, IPropertySheetPage.class);
        if (page == null) {
            page = new DefaultPropertySheetPage();
        }
        initPage(page);
        page.createControl(book);
        return page;
    }

}

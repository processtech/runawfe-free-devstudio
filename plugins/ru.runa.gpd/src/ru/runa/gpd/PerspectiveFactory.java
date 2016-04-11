package ru.runa.gpd;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ru.runa.gpd.ui.view.PropertiesView;
import ru.runa.gpd.ui.view.ValidationErrorsView;

public class PerspectiveFactory implements IPerspectiveFactory {

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        IFolderLayout propsFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.70, editorArea);
        propsFolder.addView(PropertiesView.ID);
        propsFolder.addView(ValidationErrorsView.ID);
    }

}

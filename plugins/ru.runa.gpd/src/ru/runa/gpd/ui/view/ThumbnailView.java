package ru.runa.gpd.ui.view;

import org.eclipse.graphiti.ui.internal.editor.ThumbNailView;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.util.UiUtil;

public class ThumbnailView extends ThumbNailView {

    public static final String VIEW_ID = "ru.runa.gpd.ui.view.thumbnailview"; //$NON-NLS-1$

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        UiUtil.hideToolBar(getViewSite());
    }

}

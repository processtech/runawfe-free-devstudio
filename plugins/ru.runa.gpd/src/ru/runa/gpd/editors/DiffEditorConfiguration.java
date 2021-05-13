package ru.runa.gpd.editors;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class DiffEditorConfiguration extends SourceViewerConfiguration {
    private ColorManager colorManager;

    public DiffEditorConfiguration(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new DiffEditorPropertiesReconciler(colorManager);
        return reconciler;
    }

}
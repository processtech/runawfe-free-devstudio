package ru.runa.gpd.editor;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IPasteFeature;
import org.eclipse.graphiti.features.context.IPasteContext;
import org.eclipse.graphiti.features.context.impl.PasteContext;
import org.eclipse.graphiti.internal.command.FeatureCommandWithContext;
import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
import org.eclipse.graphiti.ui.internal.action.AbstractPreDefinedAction;
import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
import org.eclipse.ui.IWorkbenchPart;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.PasteFeature;

public class CustomPasteAction extends AbstractPreDefinedAction {


    public CustomPasteAction(IWorkbenchPart editor, IConfigurationProvider configurationProvider) {
        super(editor, configurationProvider);
        setText(Localization.getString("button.paste"));
    }

    @Override
    public boolean calculateEnabled() {
        IPasteContext context = createPasteContext();
        if (!(context.getProperty(PasteFeature.PROPERTY_EDITOR) instanceof ProcessEditorBase)) {
            return false;
        }
        IFeatureProvider featureProvider = getFeatureProvider();
        if (featureProvider == null) {
            return false;
        }
        IPasteFeature feature = featureProvider.getPasteFeature(context);
        if (feature == null || !feature.canPaste(context)) {
            return false;
        }

        return getWorkbenchPart() instanceof ProcessEditorBase;
    }

    @Override
    public void run() {
        IPasteContext context = createPasteContext();
        IPasteFeature feature = getFeatureProvider().getPasteFeature(context);
        if (feature != null) {
            final FeatureCommandWithContext command = new GenericFeatureCommandWithContext(feature, context);
            executeOnCommandStack(command);
        }
    }

    private IPasteContext createPasteContext() {
        PasteContext context = new PasteContext(null);
        context.putProperty(PasteFeature.PROPERTY_EDITOR, getWorkbenchPart());
        return context;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}

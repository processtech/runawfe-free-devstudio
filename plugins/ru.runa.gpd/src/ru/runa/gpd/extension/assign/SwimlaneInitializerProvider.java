package ru.runa.gpd.extension.assign;

import org.eclipse.jface.dialogs.IDialogConstants;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.ui.dialog.SwimlaneConfigDialog;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class SwimlaneInitializerProvider extends DelegableProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        if (!HandlerArtifact.ASSIGNMENT.equals(delegable.getDelegationType())) {
            throw new IllegalArgumentException("For assignment handler only");
        }
        Swimlane swimlane = (Swimlane) delegable;
        ProcessDefinition definition = swimlane.getProcessDefinition();
        SwimlaneConfigDialog dialog = new SwimlaneConfigDialog(definition, swimlane);
        if (dialog.open() == IDialogConstants.OK_ID) {
            swimlane.setPublicVisibility(dialog.isPublicVisibility());
            swimlane.setEditorPath(dialog.getPath());
            return dialog.getConfiguration();
        }
        return null;
    }

}

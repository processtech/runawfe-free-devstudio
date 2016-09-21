package ru.runa.gpd.extension.assign;

import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.ui.dialog.SwimlaneConfigDialog;

public class SwimlaneInitializerProvider extends DelegableProvider {

    @Override
    public String showConfigurationDialog(IDelegable iDelegable) {
        if (!HandlerArtifact.ASSIGNMENT.equals(iDelegable.getDelegationType())) {
            throw new IllegalArgumentException("For assignment handler only");
        }
        Swimlane swimlane = (Swimlane) iDelegable;
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

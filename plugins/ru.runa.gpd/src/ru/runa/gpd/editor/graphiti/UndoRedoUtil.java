package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.ui.PlatformUI;

public class UndoRedoUtil {

    public static <T, V> void executeFeature(ChangePropertyFeature<T, V> feature) {
        DiagramBehavior db = ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                .getDiagramEditorPage().getDiagramBehavior();
        feature.setFeatureProvider(db.getDiagramTypeProvider().getFeatureProvider());
        db.getEditDomain().getCommandStack()
                .execute(new GefCommandWrapper(new GenericFeatureCommandWithContext(feature, new CustomContext()), db.getEditingDomain()));
    }

    private UndoRedoUtil() {
        // all-static class
    }

}

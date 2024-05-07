package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;

public class UndoRedoUtil {

    private static boolean inProgress = false; // true во время выполнения процесса undo или redo

    private UndoRedoUtil() {
        throw new IllegalStateException("Utility class not instantiable");
    }

    public static boolean isInProgress() {
        return inProgress;
    }

    public static void setInProgress(boolean inProgressValue) {
        inProgress = inProgressValue;
    }

    public static <T, V> void executeFeature(ChangePropertyFeature<T, V> feature) {
        DiagramBehavior db = ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                .getDiagramEditorPage().getDiagramBehavior();
        feature.setFeatureProvider(db.getDiagramTypeProvider().getFeatureProvider());
        db.getEditDomain().getCommandStack()
                .execute(new GefCommandWrapper(new GenericFeatureCommandWithContext(feature, new CustomContext()), db.getEditingDomain()));
    }

}

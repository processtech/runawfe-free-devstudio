package ru.runa.gpd.editor.graphiti;

import java.beans.PropertyChangeListener;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class UndoRedoUtil implements PropertyNames {

    private static boolean watching = true;

    private static final PropertyChangeListener pcl = pce -> {
        if (watching) {
            GraphElement graphElement = (GraphElement) pce.getSource();
            String propertyName = pce.getPropertyName();
            switch (pce.getPropertyName()) {
            case PROPERTY_NAME:
                executeFeature(new ChangeNameFeature((NamedGraphElement) pce.getSource(), (String) pce.getOldValue(), (String) pce.getNewValue()));
                break;
            case PROPERTY_DESCRIPTION:
                executeFeature(new ChangeDescriptionFeature((Describable) pce.getSource(), (String) pce.getOldValue(), (String) pce.getNewValue()));
                break;
            case PROPERTY_CLASS:
                executeFeature(
                        new ChangeDelegationClassNameFeature((Delegable) pce.getSource(), (String) pce.getOldValue(), (String) pce.getNewValue()));
                break;
            case PROPERTY_CONFIGURATION:
                executeFeature(new ChangeDelegationConfigurationFeature((Delegable) pce.getSource(), (String) pce.getOldValue(),
                        (String) pce.getNewValue()));
                break;
            }
        }
    };

    public static void watch() {
        watching = true;
    }

    public static void unwatch() {
        watching = false;
    }

    public static void watch(GraphElement graphElement) {
        for (GraphElement child : graphElement.getElements()) {
            child.removePropertyChangeListener(pcl);
            child.addPropertyChangeListener(pcl);
            watch(child);
        }
        graphElement.addPropertyChangeListener(pcl);
    }

    public static void unwatch(GraphElement graphElement) {
        for (GraphElement child : graphElement.getElements()) {
            child.removePropertyChangeListener(pcl);
            unwatch(child);
        }
        graphElement.removePropertyChangeListener(pcl);
    }

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

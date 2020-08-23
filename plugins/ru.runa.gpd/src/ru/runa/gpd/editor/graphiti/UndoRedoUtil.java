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
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.util.Duration;

public class UndoRedoUtil implements PropertyNames {

    private static boolean watching = true;

    private static final PropertyChangeListener pcl = pce -> {
        if (watching) {
            Object source = pce.getSource();
            Object oldValue = pce.getOldValue();
            Object newValue = pce.getNewValue();
            switch (pce.getPropertyName()) {
            case PROPERTY_NAME:
                executeFeature(new ChangeNameFeature((NamedGraphElement) source, (String) oldValue, (String) newValue));
                break;
            case PROPERTY_DESCRIPTION:
                executeFeature(new ChangeDescriptionFeature((Describable) source, (String) oldValue, (String) newValue));
                break;
            case PROPERTY_CLASS:
                executeFeature(new ChangeDelegationClassNameFeature((Delegable) source, (String) oldValue, (String) newValue));
                break;
            case PROPERTY_CONFIGURATION:
                executeFeature(new ChangeDelegationConfigurationFeature((Delegable) source, (String) oldValue, (String) newValue));
                break;
            case PROPERTY_TIMER_DELAY:
                if (source instanceof Timer) { // can be ProcessDefinition (RM1090u116)
                    executeFeature(new ChangeTimerDelayFeature((Timer) source, (Duration) oldValue, (Duration) newValue));
                }
                break;
            case PROPERTY_TIMER_ACTION:
                executeFeature(new ChangeTimerActionFeature((Timer) source, (TimerAction) oldValue, (TimerAction) newValue));
                break;
            case PROPERTY_TTL:
                executeFeature(new ChangeTtlDurationFeature((MessageNode) source, (Duration) oldValue, (Duration) newValue));
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

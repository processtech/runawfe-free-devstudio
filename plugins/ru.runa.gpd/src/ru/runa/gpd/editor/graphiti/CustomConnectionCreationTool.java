package ru.runa.gpd.editor.graphiti;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureAndContext;
import org.eclipse.graphiti.ui.internal.command.CreateConnectionCommand;
import org.eclipse.graphiti.ui.internal.editor.GFConnectionCreationTool;
import org.eclipse.graphiti.ui.internal.parts.DiagramEditPart;
import org.eclipse.graphiti.ui.internal.util.ui.PopupMenu;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.editor.graphiti.create.CreateAnnotationFeature;
import ru.runa.gpd.editor.graphiti.create.CreateDataStoreFeature;
import ru.runa.gpd.editor.graphiti.create.CreateElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateStartNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateSwimlaneFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class CustomConnectionCreationTool extends GFConnectionCreationTool {

    @Override
    protected boolean handleCreateConnection() {
        CreateConnectionCommand command = (CreateConnectionCommand) getCommand();
        if (command != null && getTargetEditPart() instanceof DiagramEditPart) { // Конец перехода указывает на пустое место диаграммы
            List<NodeTypeDefinition> definitions = Lists.newArrayList();
            for (ICreateFeature feature : command.getConfigurationProvider().getFeatureProvider().getCreateFeatures()) {
                if (feature instanceof CreateSwimlaneFeature || feature instanceof CreateStartNodeFeature || feature instanceof CreateDataStoreFeature
                        || feature instanceof CreateAnnotationFeature) {
                    continue;
                }
                if (feature instanceof CreateElementFeature) {
                    definitions.add(((CreateElementFeature) feature).getNodeDefinition());
                }

            }
            PopupMenu menu = new PopupMenu(definitions, new LabelProvider() {

                @Override
                public Image getImage(Object element) {
                    return ((NodeTypeDefinition) element).getImage(Language.BPMN.getNotation());
                }

                @Override
                public String getText(Object element) {
                    return ((NodeTypeDefinition) element).getLabel();
                }
            });

            boolean menuSelected = menu.show(Display.getCurrent().getActiveShell());
            if (!menuSelected) {
                return false;
            }
            NodeTypeDefinition targetNodeDefinition = (NodeTypeDefinition) menu.getResult();
            IFeatureAndContext featureAndContext = command.getFeaturesAndContexts()[0];
            ((CreateTransitionFeature) featureAndContext.getFeature()).setTargetNodeDefinition(targetNodeDefinition);
        }

        setCurrentCommand(command);
        executeCurrentCommand();
        eraseSourceFeedback();
        return true;
    }
}

package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class LayoutTransitionFeature extends LayoutElementFeature {

    @Override
    public boolean layout(ILayoutContext context) {
        Connection connection = (Connection) context.getPictogramElement();
        EList<ConnectionDecorator> decorators = connection.getConnectionDecorators();
        ProcessDefinition pd = ((GraphElement) getBusinessObjectForPictogramElement(connection)).getProcessDefinition();
        int i = 0;
        for (ConnectionDecorator decorator : decorators) {
            if (PropertyUtil.hasProperty(decorator, GaProperty.CLASS, GaProperty.ACTION_ICON)) {
                decorator.setLocation((1 + i++) * .1);
                decorator.setVisible(pd.isShowActions());
            }
        }
        return true;
    }

}

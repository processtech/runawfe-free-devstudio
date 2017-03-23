package ru.runa.gpd.lang.action.jpdl;

import ru.runa.gpd.lang.action.BaseBoundaryEventFeatureDelegate;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.IBoundaryEventContainer;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.jpdl.CatchEventNode;

public class CatchEventFeatureDelegate extends
		BaseBoundaryEventFeatureDelegate<IBoundaryEventContainer> {

	@Override
	protected GraphElement createBoundaryEvent() {
		return new CatchEventNode();
	}

	@Override
	protected Node getBoundaryEvent(IBoundaryEventContainer boundaryEvented) {
		return boundaryEvented.getCatchEventNodes();
	}

}

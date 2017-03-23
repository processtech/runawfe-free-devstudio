package ru.runa.gpd.lang.action;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;

public class TimerFeatureDelegate extends BaseBoundaryEventFeatureDelegate<ITimed> {
	
	@Override
	protected GraphElement createBoundaryEvent() {
		return new Timer();
	}

	@Override
	protected Node getBoundaryEvent(ITimed timed) {		
		return timed.getTimer();
	}
}

package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class AbstractEndTextDecorated extends Node implements HasTextDecorator {

	protected TextDecoratorEmulation decoratorEmulation;

	public AbstractEndTextDecorated() {
		decoratorEmulation = new TextDecoratorEmulation(this);
	}

	@Override
	protected boolean allowLeavingTransition(List<Transition> transitions) {
		return false;
	}

	@Override
	public TextDecoratorEmulation getTextDecoratorEmulation() {
		return decoratorEmulation;
	}

}

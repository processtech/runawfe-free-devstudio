package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;

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

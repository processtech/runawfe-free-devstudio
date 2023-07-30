package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.Localization;

public enum CycleType {
	forCycle,
    whileCycle;

    public String getImageName(boolean isCatch, boolean boundary) {
        return (boundary ? "boundary_" : "") + (isCatch ? "catch" : "throw") + "_" + name() + ".png";
    }

    private String label = Localization.getString("cycle.type." + name().toLowerCase());

    public final static String[] LABELS = {forCycle.label, whileCycle.label};

}

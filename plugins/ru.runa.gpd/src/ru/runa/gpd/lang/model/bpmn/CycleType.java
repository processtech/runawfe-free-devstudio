package ru.runa.gpd.lang.model.bpmn;

<<<<<<< HEAD
import ru.runa.gpd.Localization;

public enum CycleType {
	forCycle,
    whileCycle;
=======
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.Localization;

public enum CycleType {
	consistent,
    until;
>>>>>>> 6086b6774437415f2ce38ad3f8e65dbbc2f979f1

    public String getImageName(boolean isCatch, boolean boundary) {
        return (boundary ? "boundary_" : "") + (isCatch ? "catch" : "throw") + "_" + name() + ".png";
    }

    private String label = Localization.getString("cycle.type." + name().toLowerCase());

<<<<<<< HEAD
    public final static String[] LABELS = {forCycle.label, whileCycle.label};
=======
    public static String[] LABELS;

    static {
        List<String> eventTypeLabels = Lists.newArrayList();
        for (CycleType cycleType : CycleType.values()) {
            eventTypeLabels.add(cycleType.label);
        }
        LABELS = eventTypeLabels.toArray(new String[eventTypeLabels.size()]);
    }
>>>>>>> 6086b6774437415f2ce38ad3f8e65dbbc2f979f1
}

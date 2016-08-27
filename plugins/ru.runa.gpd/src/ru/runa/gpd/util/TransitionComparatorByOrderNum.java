package ru.runa.gpd.util;

import java.util.Comparator;

import ru.runa.gpd.lang.model.Transition;

public class TransitionComparatorByOrderNum implements Comparator<Transition> {

    @Override
    public int compare(Transition o1, Transition o2) {
        if (o1.getOrderNum() < o2.getOrderNum()) {
            return -1;
        } else if (o1.getOrderNum() > o2.getOrderNum()) {
            return 1;
        } else {
            return 0;
        }
    }
}

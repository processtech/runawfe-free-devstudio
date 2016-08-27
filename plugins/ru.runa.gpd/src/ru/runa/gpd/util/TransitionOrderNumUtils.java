package ru.runa.gpd.util;

import java.util.List;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Transition;

public class TransitionOrderNumUtils {
    /**
     * Parse and check orderNum from XML file
     * 
     * @param attributeValue
     *            - orderNum to set
     * @param parent
     *            - parent node for transition
     * @param transition
     *            - transition where orderNum will be set
     * @return orderNum to set
     */
    public static int parseOrderNum(String attributeValue, GraphElement parent, Transition transition) {
        int orderNum;
        try {
            orderNum = Integer.parseInt(attributeValue);
            if (orderNum < 0) {
                orderNum = 0;
            }
            List<Transition> leavingTransitions = parent.getChildren(Transition.class);
            for (Transition leavingTransition : leavingTransitions) {
                if (!leavingTransition.equals(transition) && leavingTransition.getOrderNum() == orderNum) {
                    orderNum = 0;
                    break;
                }
            }
        } catch (NumberFormatException e) {
            orderNum = 0;
        }
        return orderNum;
    }
}

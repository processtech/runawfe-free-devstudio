package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public enum TransitionColor {
    
    //
    // Souurce: https://www.w3schools.com/colors/colors_names.asp
    //
    DEFAULT(169, 169, 169), // DarkGray
    // DEFAULT(0, 127, 127),
    GREEN(34, 139, 34), // ForestGreen
    YELLOW(218, 165, 32), // GoldenRod
    // RED(255, 69, 0); // OrangeRed
    RED(205, 92, 92); // IndianRed
    
    public final int red;
    public final int green;
    public final int blue;
    
    private TransitionColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String getLabel() {
        return Localization.getString("TransitionColor.label." + name().toLowerCase());
    }
    
    public static TransitionColor findByValue(String value) {
        return valueOf(value.toUpperCase());
    }

}

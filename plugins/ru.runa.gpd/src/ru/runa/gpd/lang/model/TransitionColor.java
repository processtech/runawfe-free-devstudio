package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public enum TransitionColor {
    
    //
    // Source: https://www.w3schools.com/colors/colors_names.asp
    //
    DEFAULT(169, 169, 169), // DarkGray
    // GREEN(34, 139, 34), // ForestGreen
    GREEN(0, 128, 0), // Green
    // YELLOW(218, 165, 32), // GoldenRod
    // YELLOW(218, 165, 32), // GoldenRod
    YELLOW(184, 134, 11), // DarkGoldenRod
    // RED(255, 69, 0); // OrangeRed
    // RED(205, 92, 92); // IndianRed
    RED(255, 0, 0); // Red
    
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

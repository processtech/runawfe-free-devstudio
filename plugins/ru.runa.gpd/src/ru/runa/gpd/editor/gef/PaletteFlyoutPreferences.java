package ru.runa.gpd.editor.gef;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;

import ru.runa.gpd.Activator;

public class PaletteFlyoutPreferences implements FlyoutPreferences {
    public static final int DEFAULT_PALETTE_WIDTH = 150;

    protected static final String PALETTE_DOCK_LOCATION = "Dock location";

    protected static final String PALETTE_SIZE = "Palette Size";

    protected static final String PALETTE_STATE = "Palette state";

    public int getDockLocation() {
        int location = Activator.getDefault().getPreferenceStore().getInt(PALETTE_DOCK_LOCATION);
        if (location == 0) {
            return PositionConstants.WEST;
        }
        return location;
    }

    public int getPaletteState() {
        int state = Activator.getDefault().getPreferenceStore().getInt(PALETTE_STATE);
        if (state == 0) {
            state = FlyoutPaletteComposite.STATE_PINNED_OPEN;
        }
        return state;
    }

    public int getPaletteWidth() {
        int width = Activator.getDefault().getPreferenceStore().getInt(PALETTE_SIZE);
        if (width == 0)
            return DEFAULT_PALETTE_WIDTH;
        return width;
    }

    public void setDockLocation(int location) {
        Activator.getDefault().getPreferenceStore().setValue(PALETTE_DOCK_LOCATION, location);
    }

    public void setPaletteState(int state) {
        Activator.getDefault().getPreferenceStore().setValue(PALETTE_STATE, state);
    }

    public void setPaletteWidth(int width) {
        Activator.getDefault().getPreferenceStore().setValue(PALETTE_SIZE, width);
    }

}

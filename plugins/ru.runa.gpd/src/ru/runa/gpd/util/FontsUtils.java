package ru.runa.gpd.util;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

public class FontsUtils {

	public static String getSpaceByWidth(int width) {
		GC gc = new GC(Display.getDefault());
		int spaceWidth = gc.getAdvanceWidth(' ');
		gc.dispose();
		int spacecount = width / spaceWidth;
		StringBuilder b = new StringBuilder();
		while (spacecount-- > 0) {
			b.append(" ");
		}
		return b.toString();
	}
}

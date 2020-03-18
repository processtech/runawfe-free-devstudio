package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Localization;

public class FindElementDialog extends UserInputDialog {

	public FindElementDialog() {
		super(Localization.getString("InputElementID"));
	}
}

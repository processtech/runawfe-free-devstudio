package ru.runa.gpd.swimlane;

import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.wfe.WFEServerExecutorsImporter;

public class ExecutorGroupSwimlaneElement extends GroupSwimlaneElement {

    @Override
    public void createGUI(Composite clientArea) {
        super.createGUI(clientArea);
        SyncUIHelper.createHeader(getClientArea(), WFEServerExecutorsImporter.getInstance(), WFEConnectionPreferencePage.class, null);
    }

}

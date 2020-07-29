package ru.runa.gpd.swimlane;

import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerExecutorImporter;

public class WfeServerGroupSwimlaneElement extends GroupSwimlaneElement {

    @Override
    public void createGUI(Composite clientArea) {
        super.createGUI(clientArea);
        new WfeServerConnectorComposite(getClientArea(), WfeServerExecutorImporter.getInstance(), null);
    }

}

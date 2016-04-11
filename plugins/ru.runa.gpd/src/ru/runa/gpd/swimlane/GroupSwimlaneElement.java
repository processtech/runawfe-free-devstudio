package ru.runa.gpd.swimlane;

import org.eclipse.swt.widgets.Composite;

public class GroupSwimlaneElement extends SwimlaneElement<SwimlaneInitializer> {
    @Override
    public void createGUI(Composite clientArea) {
        createComposite(clientArea, 1);
    }

    @Override
    protected SwimlaneInitializer createNewSwimlaneInitializer() {
        throw new UnsupportedOperationException("this is the group element");
    }
}

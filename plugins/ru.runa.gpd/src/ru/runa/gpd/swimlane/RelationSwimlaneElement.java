package ru.runa.gpd.swimlane;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.ProcessDefinition;

public class RelationSwimlaneElement extends SwimlaneElement<RelationSwimlaneInitializer> {
    private View view;

    @Override
    public void open(String path, String swimlaneName, RelationSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        view.init(swimlaneInitializer);
    }
    
    @Override
    protected RelationSwimlaneInitializer createNewSwimlaneInitializer() {
        return new RelationSwimlaneInitializer();
    }

    @Override
    public void createGUI(Composite parent) {
        view = new View(parent, true, processDefinition);
        view.init(getSwimlaneInitializerNotNull());
    }

    private class View extends RelationComposite implements PropertyChangeListener {

        public View(Composite parent, boolean displayParameter, ProcessDefinition processDefinition) {
            super(parent, displayParameter, processDefinition);
        }
        
        @Override
        public void init(RelationSwimlaneInitializer swimlaneInitializer) {
            super.init(swimlaneInitializer);
            swimlaneInitializer.addPropertyChangeListener(this);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (swimlaneInitializer.isValid()) {
                fireCompletedEvent();
            }
        }
    }

}

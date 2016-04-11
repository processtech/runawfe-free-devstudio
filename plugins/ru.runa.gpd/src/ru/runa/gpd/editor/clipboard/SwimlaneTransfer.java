package ru.runa.gpd.editor.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

/**
 * @author KuchmaMA
 * 
 */
public final class SwimlaneTransfer extends CommonTransfer<Swimlane> {

    private final static class InstanceHolder {
        final static SwimlaneTransfer INSTANCE = new SwimlaneTransfer();
    }

    public static SwimlaneTransfer getInstance() {
        return getInstance(null);
    }

    public static SwimlaneTransfer getInstance(ProcessDefinition processDefinition) {
        InstanceHolder.INSTANCE.processDefinition = processDefinition;
        return InstanceHolder.INSTANCE;
    }

    private SwimlaneTransfer() {

    }

    @Override
    protected Class<? extends Swimlane> getObjectClass() {
        return Swimlane.class;
    }

    @Override
    protected void write(ObjectOutputStream out, Swimlane object) throws IOException {
        Serializator.write(out, object);
    }

    @Override
    protected void read(ObjectInputStream in, Swimlane object) throws ClassNotFoundException, IOException {
        Serializator.read(in, object, processDefinition);
    }

    protected Object readResolve() {
        return getInstance();
    }

}

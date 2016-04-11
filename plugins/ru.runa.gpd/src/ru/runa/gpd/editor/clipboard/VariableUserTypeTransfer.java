package ru.runa.gpd.editor.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;

/**
 * @author KuchmaMA
 * 
 */
public final class VariableUserTypeTransfer extends CommonTransfer<VariableUserType> {

    private final static class InstanceHolder {
        final static VariableUserTypeTransfer INSTANCE = new VariableUserTypeTransfer();
    }

    public static VariableUserTypeTransfer getInstance() {
        return getInstance(null);
    }

    public static VariableUserTypeTransfer getInstance(ProcessDefinition processDefinition) {
        InstanceHolder.INSTANCE.processDefinition = processDefinition;
        return InstanceHolder.INSTANCE;
    }

    private VariableUserTypeTransfer() {

    }

    @Override
    protected Class<? extends VariableUserType> getObjectClass() {
        return VariableUserType.class;
    }

    @Override
    protected void write(ObjectOutputStream out, VariableUserType object) throws IOException {
        Serializator.write(out, object);
    }

    @Override
    protected void read(ObjectInputStream in, VariableUserType object) throws ClassNotFoundException, IOException {
        Serializator.read(in, object, processDefinition);
    }

    protected Object readResolve() {
        return getInstance();
    }

}

package ru.runa.gpd.editor.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

/**
 * @author KuchmaMA
 * 
 */
public final class VariableTransfer extends CommonTransfer<Variable> {

    private final static class InstanceHolder {
        final static VariableTransfer INSTANCE = new VariableTransfer();
    }

    public static VariableTransfer getInstance() {
        return getInstance(null);
    }

    public static VariableTransfer getInstance(ProcessDefinition processDefinition) {
        InstanceHolder.INSTANCE.processDefinition = processDefinition;
        return InstanceHolder.INSTANCE;
    }

    private VariableTransfer() {

    }

    @Override
    protected Class<? extends Variable> getObjectClass() {
        return Variable.class;
    }

    @Override
    protected void write(ObjectOutputStream out, Variable object) throws IOException {
        Serializator.write(out, object);
    }

    @Override
    protected void read(ObjectInputStream in, Variable object) throws ClassNotFoundException, IOException {
        Serializator.read(in, object, processDefinition);
    }

    protected Object readResolve() {
        return getInstance();
    }

}

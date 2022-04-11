package ru.runa.gpd.editor.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.validation.ValidatorConfig;

/**
 * @author tarwirdur
 *
 */
public final class GlobalValidatorTransfer extends CommonTransfer<ValidatorConfig> {

    private final static class InstanceHolder {
        final static GlobalValidatorTransfer INSTANCE = new GlobalValidatorTransfer();
    }

    public static GlobalValidatorTransfer getInstance() {
        return getInstance(null);
    }

    public static GlobalValidatorTransfer getInstance(ProcessDefinition processDefinition) {
        InstanceHolder.INSTANCE.processDefinition = processDefinition;
        return InstanceHolder.INSTANCE;
    }

    private GlobalValidatorTransfer() {

    }

    @Override
    protected Class<? extends ValidatorConfig> getObjectClass() {
        return ValidatorConfig.class;
    }

    @Override
    protected void write(ObjectOutputStream out, ValidatorConfig object) throws IOException {
        Serializator.write(out, object);
    }

    @Override
    protected void read(ObjectInputStream in, ValidatorConfig object) throws ClassNotFoundException, IOException {
        Serializator.read(in, object, processDefinition);
    }

    protected Object readResolve() {
        return getInstance();
    }

}

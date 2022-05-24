package ru.runa.gpd.editor.clipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import ru.runa.gpd.lang.model.ProcessDefinition;

abstract class CommonTransfer<T> extends ByteArrayTransfer {
    private static final Log log = LogFactory.getLog(CommonTransfer.class);

    protected ProcessDefinition processDefinition;

    private final int typeId;
    private final String typeName;

    CommonTransfer() {
        typeName = getObjectClass().getSimpleName();
        typeId = registerType(typeName);
    }

    protected abstract Class<? extends T> getObjectClass();

    protected abstract void write(ObjectOutputStream out, T object) throws IOException;

    protected abstract void read(ObjectInputStream in, T object) throws ClassNotFoundException, IOException;

    @Override
    protected int[] getTypeIds() {
        return new int[] { typeId };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { typeName };
    }

    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        if (object instanceof List) {
            if (isSupportedType(transferData)) {
                @SuppressWarnings("unchecked")
                List<T> userTypes = (List<T>) object;
                try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream writeOut = new ObjectOutputStream(out);) {
                    writeOut.writeInt(userTypes.size());
                    for (T type : userTypes) {
                        write(writeOut, type);
                    }
                    writeOut.flush();
                    byte[] buffer = out.toByteArray();
                    super.javaToNative(buffer, transferData);
                } catch (IOException e) {
                    log.info("Transfer failed", e);
                }
            }
        }
    }

    @Override
    protected Object nativeToJava(TransferData transferData) {
        List<T> data = new ArrayList<>();
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer != null) {
                try (ByteArrayInputStream in = new ByteArrayInputStream(buffer); ObjectInputStream readIn = new ObjectInputStream(in)) {
                    int length = readIn.readInt();
                    for (int i = 0; i < length; i++) {
                        T object = getObjectClass().newInstance();
                        read(readIn, object);
                        data.add(object);
                    }
                } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log.info("Transfer failed", e);
                }
            }
        }
        return data;
    }

}

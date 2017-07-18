package ru.runa.gpd.editor.clipboard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Recursive serialization for {@link org.eclipse.swt.dnd.Clipboard}.
 * 
 * @author KuchmaMA
 * 
 */
final class Serializator {

    private Serializator() {
        // no action
    }

    static void write(ObjectOutputStream out, Swimlane swimlane) throws IOException {
        out.writeObject(Strings.nullToEmpty(swimlane.getEditorPath()));
        write(out, (Variable) swimlane);
    }

    static void read(ObjectInputStream in, Swimlane swimlane, ProcessDefinition processDefinition) throws IOException, ClassNotFoundException {
        swimlane.setEditorPath((String) in.readObject());
        read(in, (Variable) swimlane, processDefinition);
    }

    static void write(ObjectOutputStream out, Variable variable) throws IOException {
        out.writeObject(variable.getScriptingName());
        out.writeObject(variable.getFormat());
        out.writeBoolean(variable.isPublicVisibility());
        out.writeObject(Strings.nullToEmpty(variable.getDefaultValue()));
        out.writeObject(variable.getName());
        out.writeObject(Strings.nullToEmpty(variable.getDescription()));
        out.writeObject(variable.getUserType() == null ? "" : variable.getUserType().getName());
        out.writeBoolean(variable.isComplex());
        if (variable.isComplex()) {
            write(out, variable.getUserType());
        }
        else {
            boolean resizableVariable = variable.getProcessDefinition() != null && VariableUtils.isResizableVariable(variable);
            out.writeBoolean(resizableVariable);
            if (resizableVariable) {
                String[] componentNames = variable.getFormatComponentClassNames();
                List<VariableUserType> vuts = Lists.newArrayList();
                for (String componentName : componentNames) {
                    if (VariableUtils.isUserTypeFormat(componentName)) {
                        VariableUserType vut = variable.getProcessDefinition().getVariableUserType(componentName);
                        if (vut != null) {
                            vuts.add(vut);
                        }
                    }
                }
                out.writeInt(vuts.size());
                for (VariableUserType vut : vuts) {
                    write(out, vut);
                }
            }
        }
    }

    static void read(ObjectInputStream in, Variable variable, ProcessDefinition processDefinition) throws IOException, ClassNotFoundException {
        variable.setScriptingName((String) in.readObject());
        variable.setFormat((String) in.readObject());
        variable.setPublicVisibility(in.readBoolean());
        variable.setDefaultValue((String) in.readObject());
        variable.setName((String) in.readObject());
        variable.setDescription((String) in.readObject());
        String label = (String) in.readObject();
        if (!label.isEmpty()) {
            variable.setUserType(processDefinition.getVariableUserType(label));
        }
        if (in.readBoolean()) {
            VariableUserType type = new VariableUserType();
            read(in, type, processDefinition);
            variable.setUserType(type);
        }
        else if (in.readBoolean()) {
            int vutSize = in.readInt();
            for (int i = 0; i < vutSize; i++) {
                VariableUserType vut = new VariableUserType();
                read(in, vut, processDefinition);
                if (processDefinition.getVariableUserType(vut.getName()) == null) {
                    processDefinition.addVariableUserType(vut);
                }
            }
        }
    }

    static void write(ObjectOutputStream out, VariableUserType type) throws IOException {
        out.writeObject(type.getName());
        out.writeInt(type.getAttributes().size());
        for (Variable var : type.getAttributes()) {
            write(out, var);
        }
    }

    static void read(ObjectInputStream in, VariableUserType type, ProcessDefinition processDefinition) throws IOException, ClassNotFoundException {
        type.setName((String) in.readObject());
        int attrLength = in.readInt();
        for (int j = 0; j < attrLength; j++) {
            Variable var = new Variable();
            read(in, var, processDefinition);
            type.getAttributes().add(var);
        }
    }

}

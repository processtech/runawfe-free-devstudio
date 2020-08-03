package ru.runa.gpd.util.docx;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDoesNotExistException;
import ru.runa.wfe.var.dto.WfVariable;

public class VariableProvider implements IVariableProvider {

    @Override
    public ProcessDefinition getProcessDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getProcessDefinitionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProcessDefinitionName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getProcessId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserType getUserType(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getValue(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getValue(Class<T> arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getValueNotNull(String arg0) throws VariableDoesNotExistException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getValueNotNull(Class<T> arg0, String arg1) throws VariableDoesNotExistException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WfVariable getVariable(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WfVariable getVariableNotNull(String arg0) throws VariableDoesNotExistException {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(WfVariable iteratorVariable) {
        // TODO Auto-generated method stub
        int t = 0;
        t++;
    }

    public void remove(String iteratorVariableName) {
        // TODO Auto-generated method stub
        int t = 0;
        t++;
    }

    public Object getProperty(Object value, String variableName) {
        // TODO Auto-generated method stub
        int t = 0;
        t++;
        return null;
    }

}

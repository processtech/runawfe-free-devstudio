package ru.runa.gpd.lang.model;

public class DataFieldVariable extends Variable {

    private final String tableName;

    public DataFieldVariable(String tableName, Variable variable) {
        super(variable);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFieldScriptName() {
        return super.getScriptingName();
    }

    @Override
    public String getName() {
        return tableName + "." + super.getName();
    }

    @Override
    public String getScriptingName() {
        return tableName + "." + super.getScriptingName();
    }

}

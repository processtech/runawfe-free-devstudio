package ru.runa.gpd.util.docx;

public class PropertyResources {
    public PropertyResources(String fileName) {
        this(fileName, true, true);
    }

    public PropertyResources(String fileName, boolean required) {
        this(fileName, required, true);
    }

    public PropertyResources(String fileName, boolean required, boolean useDatabase) {
        // this.useDatabase = useDatabase;
        // this.fileName = fileName;
        // properties = ClassLoaderUtil.getProperties(fileName, required);
    }

    public String getStringProperty(String name, String defaultValue) {
        String result = null; // getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        // return result;
        return defaultValue;
    }
}

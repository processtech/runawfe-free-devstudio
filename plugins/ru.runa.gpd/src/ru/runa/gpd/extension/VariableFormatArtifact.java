package ru.runa.gpd.extension;

public class VariableFormatArtifact extends Artifact {
    private String javaClassName;

    public VariableFormatArtifact() {
    }

    public VariableFormatArtifact(boolean enabled, String className, String label, String javaClassName) {
        super(enabled, className, label);
        setJavaClassName(javaClassName);
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }
}

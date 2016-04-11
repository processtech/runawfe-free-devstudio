package ru.runa.gpd.extension;

import org.dom4j.Element;

public class VariableFormatContentProvider extends ArtifactContentProvider<VariableFormatArtifact> {
    private static final String VARIABLE_TYPE_ATTR = "variableType";

    @Override
    protected VariableFormatArtifact createArtifact() {
        return new VariableFormatArtifact();
    }

    @Override
    protected void loadArtifact(VariableFormatArtifact artifact, Element element) {
        super.loadArtifact(artifact, element);
        artifact.setJavaClassName(element.attributeValue(VARIABLE_TYPE_ATTR));
    }

    @Override
    protected void saveArtifact(VariableFormatArtifact artifact, Element element) {
        super.saveArtifact(artifact, element);
        element.addAttribute(VARIABLE_TYPE_ATTR, artifact.getJavaClassName());
    }
}

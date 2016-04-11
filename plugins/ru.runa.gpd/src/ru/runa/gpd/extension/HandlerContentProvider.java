package ru.runa.gpd.extension;

import org.dom4j.Element;

import com.google.common.base.Joiner;

public class HandlerContentProvider extends ArtifactContentProvider<HandlerArtifact> {
    private static final String CONFIGURER_ATTR = "configurer";
    private static final String TYPES_ATTR = "types";

    @Override
    protected HandlerArtifact createArtifact() {
        return new HandlerArtifact();
    }

    @Override
    protected void loadArtifact(HandlerArtifact artifact, Element element) {
        super.loadArtifact(artifact, element);
        String[] types = element.attributeValue(TYPES_ATTR).split(",");
        for (String type : types) {
            artifact.addType(type);
        }
        artifact.setConfigurerClassName(element.attributeValue(CONFIGURER_ATTR));
    }

    @Override
    protected void saveArtifact(HandlerArtifact artifact, Element element) {
        super.saveArtifact(artifact, element);
        element.addAttribute(TYPES_ATTR, Joiner.on(",").join(artifact.getTypes()));
        element.addAttribute(CONFIGURER_ATTR, artifact.getConfigurerClassName());
    }
}

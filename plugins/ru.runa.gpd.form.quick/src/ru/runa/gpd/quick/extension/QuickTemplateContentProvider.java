package ru.runa.gpd.quick.extension;

import org.dom4j.Element;

import ru.runa.gpd.extension.ArtifactContentProvider;

public class QuickTemplateContentProvider extends ArtifactContentProvider<QuickTemplateArtifact> {
	private static final String FILENAME = "fileName";
	@Override
    protected QuickTemplateArtifact createArtifact() {
        return new QuickTemplateArtifact();
    }
	
	@Override
    protected void loadArtifact(QuickTemplateArtifact artifact, Element element) {
        super.loadArtifact(artifact, element);
        artifact.setFileName(element.attributeValue(FILENAME));
    }

    @Override
    protected void saveArtifact(QuickTemplateArtifact artifact, Element element) {
        super.saveArtifact(artifact, element);
        element.addAttribute(FILENAME, artifact.getFileName());
    }
}

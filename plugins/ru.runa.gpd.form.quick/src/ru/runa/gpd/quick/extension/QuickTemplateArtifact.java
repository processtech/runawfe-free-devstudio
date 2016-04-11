package ru.runa.gpd.quick.extension;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.extension.Artifact;

public class QuickTemplateArtifact extends Artifact {
	private String fileName;
	private List<Artifact> parameters = new ArrayList<Artifact>();
	
	public QuickTemplateArtifact() {
		
	}
	
	public QuickTemplateArtifact(boolean enabled, String name, String label, String fileName) {
        super(enabled, name, label);
        setFileName(fileName);
    }
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public List<Artifact> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<Artifact> parameters) {
		this.parameters = parameters;
	}
}

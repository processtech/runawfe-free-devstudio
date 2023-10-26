package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;

public class SearchResultItem {

    protected SearchResultItem parent;
    protected IProject project;
    protected IFile definitionFile;
    protected GraphElement graphElement;
    protected List<SearchResultItem> children = new ArrayList();
    
    public SearchResultItem(IProject project, IFile defFile, GraphElement graphElement) {
        this.definitionFile = defFile;
        this.graphElement = graphElement;
        this.project = project;
    }
    
    public IFile getDefinitionFile() {
        return definitionFile;
    }
    
    public GraphElement getGraphElement() {
        return graphElement;
    }
    
    public List<SearchResultItem> getChildren() {
        return children;
    }

    public IProject getProject() {
        return project;
    }
    
    public SearchResultItem getParent() {
        return parent;
    }
    
    public SearchResultItem addChildren(IProject project, IFile defFile, GraphElement graphElement) {
        SearchResultItem item = new SearchResultItem(project, defFile, graphElement);
        item.parent = this;
        children.add(item);
        return item;
    }
    
    public String getLabel() {
        return graphElement.getLabel();
    }
    
    public boolean isProcDefinition() {
        return (this.graphElement instanceof ProcessDefinition || this.graphElement instanceof SubprocessDefinition);
    }
    
    public SearchResultItem getTopItem() {
        if (this.parent == null) {
            return this;
        }
        
        return parent.getTopItem();
    }
    
    public void buildPath(List<SearchResultItem> path) {
        path.add(this);
        if (parent != null) {
            parent.buildPath(path);
        }
    }
}

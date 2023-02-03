package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import ru.runa.gpd.lang.model.GraphElement;

public class SearchResultContentProvider implements ITreeContentProvider {
    
    private static List<SearchResultItem> rootItems = new ArrayList<>();
    
    public static void clearResults() {
        rootItems.clear();
    }
    
    public static void foundResults(List<GraphElement> source) {
        rootItems.clear();
        
        for (GraphElement element: source) {
            List<GraphElement> pathSource = buildPath(element);
            GraphElement elmSourceRoot = pathSource.get(pathSource.size() - 1);
            
            SearchResultItem itemRoot = rootItems
                .stream()
                .filter(item -> item.getGraphElement().equals(elmSourceRoot) )
                .findAny()
                .orElse(null);
            
            if (itemRoot == null) {
                itemRoot = new SearchResultItem(null, null, elmSourceRoot);
                rootItems.add(itemRoot);
            } 
            
            includeElement(itemRoot, pathSource, pathSource.size() - 1);
        }
    }
    
    private static void includeElement(SearchResultItem itemRoot, List<GraphElement> pathSource, int pathStartIndex) {
        if (pathStartIndex == 0) {
            return;
        }
        
        GraphElement elmSource = pathSource.get(pathStartIndex -1);
        
        SearchResultItem itemNewParent = itemRoot.getChildren()
            .stream()
            .filter(item -> item.getGraphElement().equals(elmSource) )
            .findAny()
            .orElse(null);
        if (itemNewParent == null) {
            itemNewParent = itemRoot.addChildren(null, null, elmSource);
        }
        includeElement(itemNewParent, pathSource, pathStartIndex - 1);
    }
    
    public static List<GraphElement> buildPath(GraphElement source) {
        List<GraphElement> lstRes = new ArrayList<>();
        while (source != null) {
            lstRes.add(source);
            source = source.getParent();
        }
        return lstRes;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        SearchResultItem[] res = new SearchResultItem[rootItems.size()];
        rootItems.toArray(res);
        return res;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        List<SearchResultItem> lstRes = ((SearchResultItem)parentElement).getChildren(); 
        return  lstRes.toArray(new SearchResultItem[lstRes.size()]);
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object parentElement) {
        return getChildren(parentElement).length > 0;
    }

}

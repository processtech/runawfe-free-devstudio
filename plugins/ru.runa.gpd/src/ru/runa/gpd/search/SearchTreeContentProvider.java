package ru.runa.gpd.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class SearchTreeContentProvider implements ITreeContentProvider, IFileSearchContentProvider {
    private final Object[] EMPTY_ARR = new Object[0];
    private SearchResult result;
    private final AbstractTreeViewer treeViewer;
    private Map<Object, Set<Object>> fChildrenMap;

    public SearchTreeContentProvider(AbstractTreeViewer viewer) {
        this.treeViewer = viewer;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Object[] children = getChildren(inputElement);
        int elementLimit = getElementLimit();
        if (elementLimit != -1 && elementLimit < children.length) {
            Object[] limitedChildren = new Object[elementLimit];
            System.arraycopy(children, 0, limitedChildren, 0, elementLimit);
            return limitedChildren;
        }
        return children;
    }

    private int getElementLimit() {
        return 1000;
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof SearchResult) {
            initialize((SearchResult) newInput);
        }
    }

    protected synchronized void initialize(SearchResult result) {
        this.result = result;
        fChildrenMap = new HashMap<Object, Set<Object>>();
        if (result != null) {
            Object[] elements = result.getElements();
            for (int i = 0; i < elements.length; i++) {
                insert(elements[i], false);
            }
        }
    }

    protected void insert(Object child, boolean refreshViewer) {
        Object parentChild = child;
        Object parent = getParent(parentChild);
        while (parent != null) {
            if (insertChild(parent, parentChild)) {
                if (refreshViewer) {
                    treeViewer.add(parent, parentChild);
                }
            } else {
                if (refreshViewer) {
                    treeViewer.refresh(parent);
                }
                return;
            }
            parentChild = parent;
            parent = getParent(parentChild);
        }
        if (insertChild(result, parentChild)) {
            if (refreshViewer) {
                treeViewer.add(result, parentChild);
            }
        }
    }

    /**
     * returns true if the child already was a child of parent.
     * 
     * @param parent
     * @param child
     * @return Returns <code>true</code> if the child was added
     */
    private boolean insertChild(Object parent, Object child) {
        Set<Object> children = fChildrenMap.get(parent);
        if (children == null) {
            children = new HashSet<Object>();
            fChildrenMap.put(parent, children);
        }
        return children.add(child);
    }

    protected void remove(Object element, boolean refreshViewer) {
        // precondition here: fResult.getMatchCount(child) <= 0
        if (hasChildren(element)) {
            if (refreshViewer) {
                treeViewer.refresh(element);
            }
        } else {
            if (result.getMatchCount(element) == 0) {
                fChildrenMap.remove(element);
                Object parent = getParent(element);
                if (parent != null) {
                    removeFromSiblings(element, parent);
                    remove(parent, refreshViewer);
                } else {
                    removeFromSiblings(element, result);
                    if (refreshViewer) {
                        treeViewer.refresh();
                    }
                }
            } else {
                if (refreshViewer) {
                    treeViewer.refresh(element);
                }
            }
        }
    }

    private void removeFromSiblings(Object element, Object parent) {
        Set<Object> siblings = fChildrenMap.get(parent);
        if (siblings != null) {
            siblings.remove(element);
        }
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        Set<Object> children = fChildrenMap.get(parentElement);
        if (children == null) {
            return EMPTY_ARR;
        }
        return children.toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    @Override
    public synchronized void elementsChanged(Object[] updatedElements) {
        for (int i = 0; i < updatedElements.length; i++) {
            if (result.getMatchCount(updatedElements[i]) > 0) {
                insert(updatedElements[i], true);
            } else {
                remove(updatedElements[i], true);
            }
        }
    }

    @Override
    public void clear() {
        initialize(result);
        treeViewer.refresh();
    }

    @Override
    public Object getParent(Object element) {
        ElementMatch elementMatch = (ElementMatch) element;
        GraphElement graphElement = elementMatch.getGraphElement();
        if (graphElement instanceof ProcessDefinition) {
            return null;
        }
        if (elementMatch.getParent() != null) {
            return elementMatch.getParent();
        }
        return new ElementMatch(graphElement.getParent(), elementMatch.getFile());
    }
}

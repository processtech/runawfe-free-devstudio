package ru.runa.gpd.editor.outline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.EmbeddedSubprocess;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GroupElement;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;
import ru.runa.gpd.util.EditorUtils;

public class FilteredTreeViewer extends AbstractEditPartViewer implements ISelectionProvider {
    private final ProcessDefinition processDefinition;
    private FilteredTree filteredTree;

    public FilteredTreeViewer(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        setRootEditPart(new RootTreeEditPart());
    }

    @Override
    public Control createControl(final Composite parent) {
        parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        final Composite mainComposite = new Composite(parent, SWT.BORDER);
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 10).create());
        mainComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        createFilteredTree(mainComposite);
        setControl(mainComposite);
        return mainComposite;
    }

    private TreeViewer createFilteredTree(final Composite mainComposite) {
        final PatternFilter filter = new SearchPatternFilter();
        filter.setIncludeLeadingWildcard(true);
        filteredTree = new FilteredTree(mainComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
        final TreeViewer treeViewer = filteredTree.getViewer();
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.setContentProvider(new TreeContentProvider());
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof Action && !processDefinition.isShowActions()) {
                    return false;
                }
                return true;
            }
        });
        filteredTree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        filteredTree.getFilterControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        filteredTree.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        filteredTree.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                GraphElement targetElement = (GraphElement) selection.getFirstElement();
                ProcessEditorBase editorPart = EditorUtils.openEditorByElement(targetElement);
                editorPart.select(targetElement);
            }
        });
        filteredTree.getViewer().setInput(processDefinition);
        return treeViewer;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public EditPart findObjectAtExcluding(final Point pt, final Collection exclude, final Conditional condition) {
        return null;
    }

    @Override
    protected void hookControl() {
        if (getControl() == null) {
            return;
        }
        super.hookControl();
    }

    @Override
    public void reveal(final EditPart part) {
    }

    @Override
    protected void unhookControl() {
        if (getControl() == null) {
            return;
        }
        super.unhookControl();
    }

    public void refresh(GraphElement graphElement) {
        filteredTree.getViewer().refresh(graphElement);
    }

    private final class SearchPatternFilter extends PatternFilter {

        @Override
        protected boolean isLeafMatch(final org.eclipse.jface.viewers.Viewer viewer, final Object element) {
            final String labelText = ((ILabelProvider) ((StructuredViewer) viewer).getLabelProvider()).getText(element);

            if (labelText == null) {
                return false;
            }
            if (wordMatches(labelText)) {
                return true;
            }
            return false;
        }
    }

    private class TreeContentProvider implements ITreeContentProvider {
        private final Object[] empty = new Object[0];

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            List<GraphElement> result = new ArrayList<>();
            if (parentElement instanceof GroupElement) {
                GroupElement groupElement = (GroupElement) parentElement;
                result.addAll(groupElement.getProcessDefinition().getChildren(groupElement.getTypeDefinition().getModelClass()));
                removeInheritanceDublicates(result, groupElement.getTypeDefinition());
                Collections.sort(result, new Comparator<GraphElement>() {

                    @Override
                    public int compare(GraphElement o1, GraphElement o2) {
                        return o1.getLabel().compareTo(o2.getLabel());
                    }

                });
            }
            if (parentElement instanceof Node) {
                Node node = (Node) parentElement;
                List<Transition> transitions = node.getLeavingTransitions();
                if (transitions.size() > 1) {
                    result.addAll(transitions);
                }
            }
            if (parentElement instanceof ActionContainer) {
                result.addAll(((ActionContainer) parentElement).getActions());
            }
            if (parentElement instanceof Subprocess) {
                Subprocess subprocess = (Subprocess) parentElement;
                if (subprocess.isEmbedded() && subprocess.getEmbeddedSubprocess() != null) {
                    addForProcessDefinition(result, subprocess.getEmbeddedSubprocess());
                }
            }
            if (parentElement instanceof ProcessDefinition) {
                addForProcessDefinition(result, (ProcessDefinition) parentElement);
            }
            if (!result.isEmpty()) {
                return result.toArray(new GraphElement[result.size()]);
            }
            return empty;
        }

        private void addForProcessDefinition(List<GraphElement> result, ProcessDefinition processDefinition) {
            result.addAll(processDefinition.getChildren(StartState.class));
            for (NodeTypeDefinition type : NodeRegistry.getDefinitions()) {
                if (StartState.class == type.getModelClass() || EndState.class == type.getModelClass()) {
                    continue;
                }
                if (processDefinition.getLanguage() == Language.JPDL && type.getJpdlElementName() == null) {
                    continue;
                }
                if (processDefinition.getLanguage() == Language.BPMN && type.getBpmnElementName() == null) {
                    continue;
                }
                List<? extends GraphElement> elements = processDefinition.getChildren(type.getModelClass());
                elements.removeIf(element -> element.getClass() != type.getModelClass());
                if (elements.size() > 0) {
                    result.add(new GroupElement(processDefinition, type));
                }
            }
            result.addAll(processDefinition.getChildren(EndState.class));
        }

        private void removeInheritanceDublicates(List<GraphElement> list, NodeTypeDefinition typeDefinition) {
            if (typeDefinition.getModelClass() == Variable.class) {
                list.removeIf(element -> element instanceof Swimlane);
            }
            if (typeDefinition.getModelClass() == Subprocess.class) {
                list.removeIf(element -> element instanceof MultiSubprocess);
                list.removeIf(element -> element instanceof EmbeddedSubprocess);
            }
            if (typeDefinition.getModelClass() == TaskState.class) {
                list.removeIf(element -> element instanceof MultiTaskState);
            }
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ProcessDefinition) {
                return null;
            }
            return ((GraphElement) element).getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

    private class TreeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return ((GraphElement) element).getLabel();
        }

        @Override
        public Image getImage(Object element) {
            GraphElement graphElement = (GraphElement) element;
            return graphElement.getEntryImage();
        }
    }
}
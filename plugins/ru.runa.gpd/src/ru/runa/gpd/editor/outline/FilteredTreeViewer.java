package ru.runa.gpd.editor.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jdt.internal.ui.packageview.DefaultElementComparer;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
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
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GroupElement;
import ru.runa.gpd.lang.model.Transition;

public class FilteredTreeViewer extends AbstractEditPartViewer implements ISelectionProvider {

    private FilteredTree filteredTree;
    private ProcessEditorBase editor;
    private boolean showActions;

    public FilteredTreeViewer(ProcessEditorBase editor) {
        final RootTreeEditPart rep = new RootTreeEditPart();
        setRootEditPart(rep);
        this.editor = editor;
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
                if (!showActions) {
                    return !(element instanceof ActionTreeEditPart || element instanceof ActionNodeTreeEditPart);
                }
                return true;
            }
        });
        treeViewer.setComparer(new IElementComparer() {

            @Override
            public boolean equals(Object a, Object b) {
                if (a instanceof EditPart && b instanceof EditPart) {
                    GraphElement firstGraphElement = (GraphElement) ((EditPart) a).getModel();
                    GraphElement secondGraphElement = (GraphElement) ((EditPart) b).getModel();
                    String firstGraphElementLabel;
                    String secondGraphElementLabel;
                    if (firstGraphElement instanceof GroupElement) {
                        firstGraphElementLabel = ((GroupElement) firstGraphElement).getTypeDefinition().getLabel();
                    } else if (firstGraphElement instanceof Transition) {
                        firstGraphElementLabel = ((Transition) firstGraphElement).toString();
                    } else {
                        firstGraphElementLabel = firstGraphElement.getLabel();
                    }
                    if (secondGraphElement instanceof GroupElement) {
                        secondGraphElementLabel = ((GroupElement) secondGraphElement).getTypeDefinition().getLabel();
                    } else if (secondGraphElement instanceof Transition) {
                        secondGraphElementLabel = ((Transition) secondGraphElement).toString();
                    } else {
                        secondGraphElementLabel = secondGraphElement.getLabel();
                    }

                    if (firstGraphElementLabel.equals(secondGraphElementLabel)) {
                        return true;
                    }
                } else {
                    DefaultElementComparer.INSTANCE.equals(a, b);
                }
                return false;
            }

            @Override
            public int hashCode(Object element) {
                GraphElement graphElement = (GraphElement) ((EditPart) element).getModel();
                if (graphElement instanceof GroupElement) {
                    return Objects.hash(((GroupElement) graphElement).getTypeDefinition().getLabel());
                } else {
                    return Objects.hash(graphElement.getLabel());
                }
            }

        });
        filteredTree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        filteredTree.getFilterControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        filteredTree.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        filteredTree.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                EditPart part = (EditPart) selection.getFirstElement();
                editor.setFocus();
                editor.select((GraphElement) part.getModel());
            }
        });
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

    @Override
    public void setContents(EditPart editpart) {
        filteredTree.getViewer().setInput(editpart);
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

    private class TreeContentProvider extends EditPartListener.Stub implements ITreeContentProvider, PropertyChangeListener {

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (oldInput != null) {
                removeListenerFrom((EditPart) oldInput);
            }
            if (newInput != null) {
                isShowActions((EditPart) newInput);
                addListenerTo((EditPart) newInput);
            }
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
            if (parentElement instanceof EditPart) {
                EditPart part = (EditPart) parentElement;
                List<EditPart> children = part.getChildren();
                return concat(children.toArray());
            }
            Object[] empty = new Object[0];
            return empty;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof EditPart) {
                return ((EditPart) element).getParent();
            }
            return null;
        }

        @Override
        public void childAdded(EditPart child, int index) {
            addListenerTo(child);
        }

        @Override
        public void partDeactivated(EditPart editpart) {
            removeListenerFrom(editpart);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String messageId = evt.getPropertyName();
            if (PropertyNames.PROPERTY_EDIT_PART_UPDATED.equals(messageId)) {
                filteredTree.getViewer().refresh();
            }
            if (PropertyNames.PROPERTY_SHOW_ACTIONS.equals(messageId)) {
                showActions = (boolean) evt.getNewValue();
                filteredTree.getViewer().refresh();
            }
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        private Object[] concat(Object[]... arrays) {
            return Stream.of(arrays).flatMap(Stream::of).toArray(Object[]::new);
        }

        private void addListenerTo(EditPart part) {
            if (!(part instanceof OutlineRootTreeEditPart)) {
                ((ElementTreeEditPart) part).addPropertyChangeListener(this);
                part.addEditPartListener(this);
                if (part instanceof ProcessDefinitionTreeEditPart) {
                    ((ProcessDefinitionTreeEditPart) part).getModel().addPropertyChangeListener(this);
                }
            }
            if (part.getChildren() != null) {
                for (Object childPart : part.getChildren()) {
                    addListenerTo((EditPart) childPart);
                }
            }
        }

        private void removeListenerFrom(EditPart part) {
            if (!(part instanceof OutlineRootTreeEditPart)) {
                ((ElementTreeEditPart) part).removePropertyChangeListener(this);
                part.removeEditPartListener(this);
                if (part instanceof ProcessDefinitionTreeEditPart) {
                    ((ProcessDefinitionTreeEditPart) part).getModel().removePropertyChangeListener(this);
                }
            }
            if (part.getChildren() != null) {
                for (Object childPart : part.getChildren()) {
                    removeListenerFrom((EditPart) childPart);
                }
            }
        }

        private void isShowActions(EditPart editPart) {
            for (Object childPart: editPart.getChildren()) {
                if (childPart instanceof ProcessDefinitionTreeEditPart) {
                    showActions = ((ProcessDefinitionTreeEditPart) childPart).getModel().isShowActions();
                }
            }
        }
    }

    private class TreeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof GroupElementTreeEditPart) {
                return ((GroupElementTreeEditPart) element).getModel().getTypeDefinition().getLabel();
            }
            return ((ElementTreeEditPart) element).getModel().getLabel();
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof GroupElementTreeEditPart) {
                return SharedImages.getImage("icons/obj/group.gif");
            }
            return ((ElementTreeEditPart) element).getModel().getEntryImage();
        }
    }
}
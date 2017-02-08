package ru.runa.gpd.lang.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.decision.IDecisionProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.TransitionOrderNumCellEditorValidator;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Transition extends NamedGraphElement implements Active {
    private Node target;
    private List<Point> bendpoints = Lists.newArrayList();
    private boolean exclusiveFlow;
    private boolean defaultFlow;
    private Point labelLocation;

    public Point getLabelLocation() {
        return labelLocation;
    }

    public void setLabelLocation(Point labelLocation) {
        if (!Objects.equal(this.labelLocation, labelLocation)) {
            Point old = this.labelLocation;
            this.labelLocation = labelLocation;
            firePropertyChange(TRANSITION_LABEL_LOCATION_CHANGED, old, labelLocation);
        }
    }

    public List<Point> getBendpoints() {
        return bendpoints;
    }

    public void setBendpoints(List<Point> bendpoints) {
        this.bendpoints = bendpoints;
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, 1);
    }

    public void addBendpoint(int index, Point bendpoint) {
        getBendpoints().add(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void removeBendpoint(int index) {
        getBendpoints().remove(index);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public void setBendpoint(int index, Point bendpoint) {
        getBendpoints().set(index, bendpoint);
        firePropertyChange(TRANSITION_BENDPOINTS_CHANGED, null, index);
    }

    public boolean isExclusiveFlow() {
        return exclusiveFlow;
    }

    public boolean isDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(boolean defaultFlow) {
        if (this.defaultFlow != defaultFlow) {
            this.defaultFlow = defaultFlow;
            firePropertyChange(TRANSITION_FLOW, !defaultFlow, defaultFlow);
        }
    }

    public void setExclusiveFlow(boolean exclusiveFlow) {
        if (this.exclusiveFlow != exclusiveFlow) {
            this.exclusiveFlow = exclusiveFlow;
            firePropertyChange(TRANSITION_FLOW, !exclusiveFlow, exclusiveFlow);
        }
    }

    @Override
    public void setName(String newName) {
        String oldName = getName();
        Node source = getSource();
        if (source == null) {
            return;
        }
        List<Transition> list = source.getLeavingTransitions();
        for (Transition transition : list) {
            if (Objects.equal(newName, transition.getName())) {
                return;
            }
        }
        super.setName(newName);
        if (oldName != null && source instanceof Decision) {
            Decision decision = (Decision) source;
            IDecisionProvider provider = HandlerRegistry.getProvider(decision);
            provider.transitionRenamed(decision, oldName, getName());
        }
        if (oldName != null && source instanceof FormNode && source.getLeavingTransitions().size() > 1) {
            FormNode formNode = (FormNode) source;
            IFile file = EditorUtils.getCurrentEditor().getDefinitionFile();
            FormNodeValidation validation = formNode.getValidation(file);
            boolean changed = false;
            for (Map<String, ValidatorConfig> map : validation.getFieldConfigs().values()) {
                for (ValidatorConfig config : map.values()) {
                    if (config.getTransitionNames().remove(oldName)) {
                        config.getTransitionNames().add(newName);
                        changed = true;
                    }
                }
            }
            if (changed) {
                ValidationUtil.rewriteValidation(file, formNode, validation);
            }
        }
    }

    public Node getSource() {
        return (Node) getParent();
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        Node old = this.target;
        this.target = target;
        if (old != null) {
            old.firePropertyChange(NODE_ARRIVING_TRANSITION_REMOVED, null, this);
        }
        if (this.target != null) {
            this.target.firePropertyChange(NODE_ARRIVING_TRANSITION_ADDED, null, this);
        }
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_SOURCE, Localization.getString("Transition.property.source")));
        descriptors.add(new PropertyDescriptor(PROPERTY_TARGET, Localization.getString("Transition.property.target")));
        if (getSource() instanceof TaskState) {
            if (getSource().getLeavingTransitions().size() == 1) {
                descriptors.add(new PropertyDescriptor(PROPERTY_ORDERNUM, Localization.getString("Transition.property.orderNum")));
            } else {
                TextPropertyDescriptor orderNumPropertyDescriptor = new TextPropertyDescriptor(PROPERTY_ORDERNUM,
                        Localization.getString("Transition.property.orderNum"));
                orderNumPropertyDescriptor.setValidator(new TransitionOrderNumCellEditorValidator(getSource().getLeavingTransitions().size()));
                descriptors.add(orderNumPropertyDescriptor);
            }
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SOURCE.equals(id) && getSource() != null) {
            return getSource().getName();
        } else if (PROPERTY_TARGET.equals(id) && getTarget() != null) {
            return target != null ? target.getName() : "";
        } else if (PROPERTY_ORDERNUM.equals(id)) {
            return getSource() instanceof TaskState ? Integer.toString(getSource().getLeavingTransitions().indexOf(this) + 1) : null;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public String toString() {
        if (getParent() == null || target == null) {
            return "not_completed";
        }
        return getParent().toString() + " -> (" + getName() + ") -> " + target.toString();
    }

    public String getLabel() {
        StringBuilder result = new StringBuilder();
        if (getSource() instanceof ExclusiveGateway) {
            if (((ExclusiveGateway) getSource()).isDecision()) {
                result.append(getName());
            }
        } else if (getSource() instanceof Decision) {
            result.append(getName());
        } else if (PluginConstants.TIMER_TRANSITION_NAME.equals(getName())) {
            Timer timer = null;
            if (getSource() instanceof Timer) {
                timer = (Timer) getSource();
            }
            if (getSource() instanceof ITimed) {
                timer = ((ITimed) getSource()).getTimer();
            }
            if (timer != null) {
                result.append(timer.getDelay().toString());
            }
        } else if (getSource() instanceof TaskState || getSource() instanceof StartState) {
            int count = 0;
            for (Transition transition : getSource().getLeavingTransitions()) {
                if (!PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
                    count++;
                }
            }
            if (count > 1) {
                result.append(getName());
            }
        }
        return result.toString();
    }

    @Override
    public Transition getCopy(GraphElement parent) {
        Transition copy = (Transition) super.getCopy(parent);
        for (Point bp : getBendpoints()) {
            // a little shift for making visible copy on same diagram
            // synchronized with ru.runa.gpd.lang.model.GraphElement.getCopy(GraphElement)
            Point pointCopy = bp.getCopy();
            pointCopy.x += GEFConstants.GRID_SIZE;
            pointCopy.y += GEFConstants.GRID_SIZE;
            copy.getBendpoints().add(pointCopy);
        }
        if (labelLocation != null) {
            copy.setLabelLocation(labelLocation.getCopy());
        }
        ((Node) parent).onLeavingTransitionAdded(copy);
        return copy;
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_ORDERNUM.equals(id)) {
            Object oldOrderNum = getPropertyValue(PROPERTY_ORDERNUM);
            Transition anotherTransition = getSource().getLeavingTransitions().get(Integer.parseInt((String) value) - 1);
            getSource().swapChilds(this, anotherTransition);
            firePropertyChange(PROPERTY_ORDERNUM, oldOrderNum, value);
            anotherTransition.firePropertyChange(PROPERTY_ORDERNUM, value, oldOrderNum);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}

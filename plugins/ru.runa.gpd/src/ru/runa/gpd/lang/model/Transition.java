package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.TooltipBuilderHelper;
import ru.runa.gpd.editor.graphiti.change.ChangeOrderNumFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeTransitionColorFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.decision.IDecisionProvider;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.TransitionOrderNumCellEditorValidator;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;

public class Transition extends AbstractTransition implements ActionContainer {
    private boolean exclusiveFlow;
    private boolean defaultFlow;
    private Point labelLocation;
    private TransitionColor color = TransitionColor.DEFAULT;

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

    public boolean isExclusiveFlow() {
        return exclusiveFlow;
    }

    public boolean isDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(boolean defaultFlow) {
        if (this.defaultFlow != defaultFlow) {
            this.defaultFlow = defaultFlow;
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
        if (nameValidator().isValid(newName) != null) {
            return;
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
            for (ValidatorConfig config : validation.getGlobalConfigs()) {
                if (config.getTransitionNames().remove(oldName)) {
                    config.getTransitionNames().add(newName);
                    changed = true;
                    break;
                }
            }
            if (!changed) {
                test: for (Map<String, ValidatorConfig> map : validation.getFieldConfigs().values()) {
                    for (ValidatorConfig config : map.values()) {
                        if (config.getTransitionNames().remove(oldName)) {
                            config.getTransitionNames().add(newName);
                            changed = true;
                            break test;
                        }
                    }
                }
            }
            if (changed) {
                ValidationUtil.rewriteValidation(file, formNode, validation);
            }
        }
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_SOURCE, Localization.getString("Transition.property.source")));
        descriptors.add(new PropertyDescriptor(PROPERTY_TARGET, Localization.getString("Transition.property.target")));
        if (getSource().getLeavingTransitions().size() == 1) {
            descriptors.add(new PropertyDescriptor(PROPERTY_ORDERNUM, Localization.getString("Transition.property.orderNum")));
        } else {
            TextPropertyDescriptor orderNumPropertyDescriptor = new TextPropertyDescriptor(PROPERTY_ORDERNUM,
                    Localization.getString("Transition.property.orderNum"));
            orderNumPropertyDescriptor.setValidator(new TransitionOrderNumCellEditorValidator(getSource().getLeavingTransitions().size()));
            descriptors.add(orderNumPropertyDescriptor);
        }
        if (FormNode.class.isAssignableFrom(getSource().getClass()) && getProcessDefinition().getLanguage().equals(Language.BPMN)) {
                descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_COLOR, Localization.getString("Transition.property.color"),
                        Stream.of(TransitionColor.values()).map(e -> e.getLabel()).toArray(String[]::new)));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SOURCE.equals(id) && getSource() != null) {
            return getSource().getName();
        } else if (PROPERTY_TARGET.equals(id) && getTarget() != null) {
            return target != null ? target.getName() : "";
        } else if (PROPERTY_ORDERNUM.equals(id)) {
            return Integer.toString(getSource().getLeavingTransitions().indexOf(this) + 1);
        } else if (PROPERTY_COLOR.equals(id)) {
            return getSource() instanceof FormNode ? getColor().ordinal() : null;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public String getLabel() {
        Node source = getSource();
        if (source instanceof ExclusiveGateway) {
            if (((ExclusiveGateway) source).isDecision()) {
                return getName();
            }
        } else if (source instanceof Decision) {
            return getName();
        } else if (PluginConstants.TIMER_TRANSITION_NAME.equals(getName())) {
            Timer timer = null;
            if (source instanceof Timer) {
                timer = (Timer) source;
            }
            if (source instanceof ITimed) {
                timer = ((ITimed) source).getTimer();
            }
            if (timer != null) {
                return timer.getDelay().toString();
            }
        } else {
            if (source instanceof FormNode) {
                TaskStateExecutionButton sourceExecutionButton = ((FormNode) source).getExecutionButton();
                if (sourceExecutionButton == TaskStateExecutionButton.BY_LEAVING_TRANSITION_NAME
                        || sourceExecutionButton == TaskStateExecutionButton.NONE
                                && source.getProcessDefinition().getExecutionButton() == TaskStateExecutionButton.BY_LEAVING_TRANSITION_NAME) {
                    return getName();
                }
            }
            if (source instanceof TaskState || source instanceof StartState) {
                int count = 0;
                for (Transition transition : source.getLeavingTransitions()) {
                    if (!PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
                        count++;
                    }
                }
                if (count > 1) {
                    return getName();
                }
            }
        }
        return "";
    }

    @Override
    protected void fillCopyCustomFields(GraphElement aCopy) {
        super.fillCopyCustomFields(aCopy);
        Transition copy = (Transition) aCopy;
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
        copy.setColor(getColor());
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_ORDERNUM.equals(id)) {
            UndoRedoUtil.executeFeature(new ChangeOrderNumFeature(this, value));
        } else if (PROPERTY_COLOR.equals(id)) {
            UndoRedoUtil.executeFeature(new ChangeTransitionColorFeature(this, TransitionColor.values()[(int) value]));
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public TransitionColor getColor() {
        return color;
    }

    public void setColor(TransitionColor color) {
        if (this.color != color) {
            TransitionColor oldColor = this.color;
            this.color = color;
            firePropertyChange(PROPERTY_COLOR, oldColor, this.color);
        }
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (getName() == null) {
            errors.add(ValidationError.createLocalizedError(this, "nameNotDefined"));
        }
    }

    @Override
    public IInputValidator nameValidator() {
        return (String name) -> {
            String parentError = super.nameValidator().isValid(name);
            if (parentError != null) {
                return parentError;
            }
            name = name.trim();
            List<Transition> list = getSource().getLeavingTransitions();
            for (Transition transition : list) {
                if (Objects.equal(name, transition.getName())) {
                    return Localization.getString("error.transition_already_exists", name);
                }
            }
            return null;
        };

    }

    @Override
    public String getTooltip() {
        if (this.getParent().getChildren(Transition.class).size() > 1) {
            StringBuilder tooltipBuilder = new StringBuilder(super.getTooltip());
            Object orderNum = getPropertyValue(PropertyNames.PROPERTY_ORDERNUM);
            tooltipBuilder.append(TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + Localization.getString("Transition.property.orderNum")
                    + TooltipBuilderHelper.COLON + TooltipBuilderHelper.SPACE + orderNum);
            return tooltipBuilder.toString();
        }
        return super.getTooltip();
    }

}

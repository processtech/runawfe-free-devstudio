package ru.runa.gpd.lang.model;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.EmbeddedSubprocess.Behavior;
import ru.runa.gpd.util.Duration;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class SubprocessDefinition extends ProcessDefinition {
    private Behavior behavior = Behavior.GraphPart;

    public SubprocessDefinition(IFile file) {
        super(file);
        setAccessType(ProcessDefinitionAccessType.EmbeddedSubprocess);
    }

    @Override
    public ProcessDefinition getMainProcessDefinition() {
        return getParent().getMainProcessDefinition();
    };

    @Override
    public Duration getDefaultTaskTimeoutDelay() {
        return getParent().getDefaultTaskTimeoutDelay();
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_ACCESS_TYPE.equals(id)) {
            return Localization.getString("ProcessDefinition.property.accessType.EmbeddedSubprocess");
        }
        if (PROPERTY_BEHAVIOR.equals(id)) {
            return Localization.getString(P_EMBEDDED_SUBPROCESS_BEHAVIOR + "." + behavior);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public boolean isShowActions() {
        return getParent().isShowActions();
    }

    @Override
    public void setShowActions(boolean showActions) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public boolean isShowGrid() {
        return getParent().isShowGrid();
    }

    @Override
    public void setShowGrid(boolean showGrid) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public Language getLanguage() {
        return getParent().getLanguage();
    }

    @Override
    public void setLanguage(Language language) {
        throw new UnsupportedOperationException("This property is inherited from main process definition");
    }

    @Override
    public List<Swimlane> getSwimlanes() {
        return getParent().getSwimlanes();
    }

    @Override
    public List<String> getVariableNames(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        return getParent().getVariableNames(expandComplexTypes, includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        return getParent().getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters);
    }

    @Override
    public List<VariableUserType> getVariableUserTypes() {
        return getParent().getVariableUserTypes();
    }

    @Override
    public void addVariableUserType(VariableUserType type) {
        getParent().addVariableUserType(type);
    }

    @Override
    public void removeVariableUserType(VariableUserType type) {
        getParent().removeVariableUserType(type);
    }

    @Override
    public VariableUserType getVariableUserType(String name) {
        return getParent().getVariableUserType(name);
    }

    @Override
    public VariableUserType getVariableUserTypeNotNull(String name) {
        return getParent().getVariableUserTypeNotNull(name);
    }

    @Override
    public Swimlane getSwimlaneByName(String name) {
        return getParent().getSwimlaneByName(name);
    }

    @Override
    public String getNextSwimlaneName() {
        return getParent().getNextSwimlaneName();
    }

    @Override
    public void addChild(GraphElement child, int index) {
        if (child instanceof Variable || child instanceof Swimlane) {
            getParent().addChild(child);
            return;
        }
        super.addChild(child, index);
    }

    @Override
    public void removeChild(GraphElement child) {
        if (child instanceof Variable || child instanceof Swimlane) {
            getParent().removeChild(child);
            return;
        }
        super.removeChild(child);
    }

    @Override
    public NodeTypeDefinition getTypeDefinition() {
        return NodeRegistry.getNodeTypeDefinition(ProcessDefinition.class);
    }

    @Override
    public ProcessDefinition getParent() {
        return (ProcessDefinition) super.getParent();
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        List<StartState> startStates = getChildren(StartState.class);
        if (startStates.size() == 1 && startStates.get(0).getLeavingTransitions().size() != 1) {
            errors.add(ValidationError.createLocalizedError(startStates.get(0), "subprocess.embedded.startState.required1leavingtransition"));
        }
        if (behavior == Behavior.GraphPart) {
            List<EndState> endStates = getChildren(EndState.class);
            for (EndState endState : endStates) {
                errors.add(ValidationError.createLocalizedError(endState, "subprocess.embedded.endState.notAllowed"));
            }
        }
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void setBehavior(Behavior behavior) {
        Behavior old = this.behavior;
        this.behavior = behavior;
        firePropertyChange(PROPERTY_BEHAVIOR, old, behavior);
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_BEHAVIOR, Localization.getString(P_EMBEDDED_SUBPROCESS_BEHAVIOR)));
    }
}

package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.StartImagePropertyDescriptor;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

@SuppressWarnings("unchecked")
public class ProcessDefinition extends NamedGraphElement implements Describable {
    private Language language;
    private NodeAsyncExecution defaultNodeAsyncExecution = NodeAsyncExecution.DEFAULT;
    private Dimension dimension;
    private boolean dirty;
    private boolean showActions;
    private boolean showGrid;
    private Duration defaultTaskTimeoutDelay = new Duration();
    private boolean invalid;
    private int nextNodeIdCounter;
    private SwimlaneDisplayMode swimlaneDisplayMode = SwimlaneDisplayMode.none;
    private final Map<String, SubprocessDefinition> embeddedSubprocesses = Maps.newHashMap();
    private ProcessDefinitionAccessType accessType = ProcessDefinitionAccessType.Process;
    private final List<VariableUserType> types = Lists.newArrayList();
    private final IFile file;
    private int hash32 = -1;

    private final ArrayList<VersionInfo> versionInfoList;

    public ProcessDefinition(IFile file) {
        this.file = file;
        versionInfoList = new ArrayList<>();
    }

    public ProcessDefinitionAccessType getAccessType() {
        return accessType;
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("composition".equals(name)) {
            return this instanceof SubprocessDefinition;
        }
        if ("hasFormCSS".equals(name)) {
            try {
                IFile file = IOUtils.getAdjacentFile(ProcessCache.getProcessDefinitionFile(this), ParContentProvider.FORM_CSS_FILE_NAME);
                return Objects.equal(value, String.valueOf(file.exists()));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("testAttribute: hasFormCSS", e);
                return false;
            }
        }
        if ("hasFormJS".equals(name)) {
            try {
                IFile file = IOUtils.getAdjacentFile(ProcessCache.getProcessDefinitionFile(this), ParContentProvider.FORM_JS_FILE_NAME);
                return Objects.equal(value, String.valueOf(file.exists()));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("testAttribute: hasFormJS", e);
                return false;
            }
        }
        return super.testAttribute(target, name, value);
    }

    public void setAccessType(ProcessDefinitionAccessType accessType) {
        this.accessType = accessType;
        firePropertyChange(PROPERTY_ACCESS_TYPE, null, accessType);
    }

    public void addEmbeddedSubprocess(SubprocessDefinition subprocessDefinition) {
        embeddedSubprocesses.put(subprocessDefinition.getId(), subprocessDefinition);
    }

    public ProcessDefinition getMainProcessDefinition() {
        return this;
    }

    public SubprocessDefinition getEmbeddedSubprocessByName(String name) {
        for (SubprocessDefinition subprocessDefinition : embeddedSubprocesses.values()) {
            if (Objects.equal(subprocessDefinition.getName(), name)) {
                return subprocessDefinition;
            }
        }
        return null;
    }

    public Map<String, SubprocessDefinition> getEmbeddedSubprocesses() {
        return embeddedSubprocesses;
    }

    public SubprocessDefinition getEmbeddedSubprocessById(String id) {
        return embeddedSubprocesses.get(id);
    }

    public SwimlaneDisplayMode getSwimlaneDisplayMode() {
        return swimlaneDisplayMode;
    }

    public void setSwimlaneDisplayMode(SwimlaneDisplayMode swimlaneDisplayMode) {
        this.swimlaneDisplayMode = swimlaneDisplayMode;
    }

    public Duration getDefaultTaskTimeoutDelay() {
        return defaultTaskTimeoutDelay;
    }

    public void setDefaultTaskTimeoutDelay(Duration defaultTaskTimeoutDelay) {
        Duration old = this.defaultTaskTimeoutDelay;
        this.defaultTaskTimeoutDelay = defaultTaskTimeoutDelay;
        firePropertyChange(PROPERTY_TASK_DEADLINE, old, defaultTaskTimeoutDelay);
    }

    public NodeAsyncExecution getDefaultNodeAsyncExecution() {
        return defaultNodeAsyncExecution;
    }

    public void setDefaultNodeAsyncExecution(NodeAsyncExecution defaultNodeAsyncExecution) {
        NodeAsyncExecution old = this.defaultNodeAsyncExecution;
        this.defaultNodeAsyncExecution = defaultNodeAsyncExecution;
        firePropertyChange(PROPERTY_NODE_ASYNC_EXECUTION, old, this.defaultNodeAsyncExecution);
    }

    public boolean isShowActions() {
        return showActions;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setShowActions(boolean showActions) {
        boolean stateChanged = this.showActions != showActions;
        this.showActions = showActions;
        if (stateChanged) {
            firePropertyChange(PROPERTY_SHOW_ACTIONS, !this.showActions, this.showActions);
            setDirty();
        }
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        boolean stateChanged = this.showGrid != showGrid;
        if (stateChanged) {
            this.showGrid = showGrid;
            firePropertyChange(PROPERTY_SHOW_GRID, !this.showGrid, this.showGrid);
            setDirty();
        }
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        boolean stateChanged = this.dirty != dirty;
        if (stateChanged) {
            this.dirty = dirty;
            firePropertyChange(PROPERTY_DIRTY, !this.dirty, this.dirty);
        }
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Dimension getDimension() {
        if (dimension == null) {
            dimension = new Dimension(0, 0);
        }
        return dimension;
    }

    public void setNextNodeIdIfApplicable(String nodeId) {
        int nextNodeId = 0;
        int dotIndex = nodeId.lastIndexOf(".");
        if (dotIndex != -1) {
            nodeId = nodeId.substring(dotIndex + 1);
        }
        if (nodeId.startsWith("ID")) {
            nodeId = nodeId.substring(2);
        }
        try {
            nextNodeId = Integer.parseInt(nodeId);
        } catch (NumberFormatException e) {
        }
        if (nextNodeId > this.nextNodeIdCounter) {
            this.nextNodeIdCounter = nextNodeId;
        }
    }

    public String getNextNodeId() {
        nextNodeIdCounter++;
        String nextNodeId = "ID" + nextNodeIdCounter;
        if (this instanceof SubprocessDefinition) {
            nextNodeId = getId() + "." + nextNodeId;
        }
        return nextNodeId;
    }

    @Override
    public void setName(String name) {
        if (name.length() == 0) {
            name = "Process";
        }
        super.setName(name);
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        List<StartState> startStates = getChildren(StartState.class);
        if (startStates.size() == 0) {
            errors.add(ValidationError.createLocalizedError(this, "startState.doesNotExist"));
        }
        if (startStates.size() > 1) {
            errors.add(ValidationError.createLocalizedError(this, "multipleStartStatesNotAllowed"));
        }
        this.invalid = false;
        for (ValidationError validationError : errors) {
            if (validationError.getSeverity() == IMarker.SEVERITY_ERROR) {
                this.invalid = true;
                break;
            }
        }
    }

    public List<String> getVariableNames(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        return VariableUtils.getVariableNames(getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters));
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        List<Variable> variables = getChildren(Variable.class);
        if (!includeSwimlanes) {
            variables.removeAll(getSwimlanes());
        }
        if (expandComplexTypes) {
            for (Variable variable : Lists.newArrayList(variables)) {
                if (variable.isComplex()) {
                    variables.addAll(VariableUtils.expandComplexVariable(variable, variable));
                }
            }
        }
        List<Variable> result = Lists.newArrayList();
        for (Variable variable : variables) {
            if (VariableFormatRegistry.isApplicable(variable, typeClassNameFilters)) {
                result.add(variable);
            }
        }
        return result;
    }

    public List<Swimlane> getSwimlanes() {
        return getChildren(Swimlane.class);
    }

    public Swimlane getSwimlaneByName(String name) {
        if (name == null) {
            return null;
        }
        List<Swimlane> swimlanes = getSwimlanes();
        for (Swimlane swimlane : swimlanes) {
            if (name.equals(swimlane.getName())) {
                return swimlane;
            }
        }
        return null;
    }

    public String getNextSwimlaneName() {
        int runner = 1;
        while (true) {
            String candidate = Localization.getString("default.swimlane.name") + runner;
            if (getSwimlaneByName(candidate) == null) {
                return candidate;
            }
            runner++;
        }
    }

    public <T extends GraphElement> T getGraphElementById(String nodeId) {
        for (GraphElement graphElement : getElementsRecursive()) {
            if (Objects.equal(nodeId, graphElement.getId())) {
                return (T) graphElement;
            }
        }
        return null;
    }

    public <T extends GraphElement> T getGraphElementByIdNotNull(String nodeId) {
        T node = ((T) getGraphElementById(nodeId));
        if (node == null) {
            List<String> nodeIds = new ArrayList<String>();
            for (Node childNode : getChildren(Node.class)) {
                nodeIds.add(childNode.getId());
            }
            throw new RuntimeException("Node not found in process definition: " + nodeId + ", all nodes: " + nodeIds);
        }
        return node;
    }

    public List<Node> getNodesRecursive() {
        return getChildrenRecursive(Node.class);
    }

    public List<GraphElement> getElementsRecursive() {
        return getChildrenRecursive(GraphElement.class);
    }

    public List<GraphElement> getContainerElements(GraphElement parentContainer) {
        List<GraphElement> list = Lists.newArrayList();
        for (GraphElement graphElement : getElementsRecursive()) {
            if (Objects.equal(parentContainer, graphElement.getParentContainer())) {
                list.add(graphElement);
            }
            if (parentContainer == this && graphElement.getParentContainer() == null) {
                list.add(graphElement);
            }
        }
        return list;
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (this instanceof SubprocessDefinition) {
            descriptors.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
            descriptors.add(new PropertyDescriptor(PROPERTY_TASK_DEADLINE, Localization.getString("default.task.deadline")));
            descriptors.add(new PropertyDescriptor(PROPERTY_ACCESS_TYPE, Localization.getString("ProcessDefinition.property.accessType")));
        } else {
            descriptors.add(new StartImagePropertyDescriptor("startProcessImage", Localization.getString("ProcessDefinition.property.startImage")));
            descriptors.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
            descriptors.add(new DurationPropertyDescriptor(PROPERTY_TASK_DEADLINE, this, getDefaultTaskTimeoutDelay(), Localization
                    .getString("default.task.deadline")));
            String[] array = { Localization.getString("ProcessDefinition.property.accessType.Process"),
                    Localization.getString("ProcessDefinition.property.accessType.OnlySubprocess") };
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_ACCESS_TYPE, Localization.getString("ProcessDefinition.property.accessType"),
                    array));
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_NODE_ASYNC_EXECUTION, Localization
                    .getString("ProcessDefinition.property.nodeAsyncExecution"), NodeAsyncExecution.LABELS));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_LANGUAGE.equals(id)) {
            return language;
        }
        if (PROPERTY_TASK_DEADLINE.equals(id)) {
            if (defaultTaskTimeoutDelay.hasDuration()) {
                return defaultTaskTimeoutDelay;
            }
            return "";
        }
        if (PROPERTY_ACCESS_TYPE.equals(id)) {
            return accessType.ordinal();
        }
        if (PROPERTY_NODE_ASYNC_EXECUTION.equals(id)) {
            return defaultNodeAsyncExecution.ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TASK_DEADLINE.equals(id)) {
            setDefaultTaskTimeoutDelay((Duration) value);
        } else if (PROPERTY_ACCESS_TYPE.equals(id)) {
            int i = ((Integer) value).intValue();
            setAccessType(ProcessDefinitionAccessType.values()[i]);
        } else if (PROPERTY_NODE_ASYNC_EXECUTION.equals(id)) {
            setDefaultNodeAsyncExecution(NodeAsyncExecution.values()[(Integer) value]);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/process.gif");
    }

    public List<VariableUserType> getVariableUserTypes() {
        return types;
    }

    public void addVariableUserType(VariableUserType type) {
        type.setProcessDefinition(this);
        types.add(type);
        firePropertyChange(PROPERTY_USER_TYPES_CHANGED, null, type);
    }

    public void changeVariableUserTypePosition(VariableUserType type, int position) {
        if (position != -1 && types.remove(type)) {
            types.add(position, type);
            firePropertyChange(PROPERTY_USER_TYPES_CHANGED, null, type);
        }
    }

    public void removeVariableUserType(VariableUserType type) {
        types.remove(type);
        firePropertyChange(PROPERTY_USER_TYPES_CHANGED, null, type);
    }

    public VariableUserType getVariableUserType(String name) {
        for (VariableUserType type : types) {
            if (Objects.equal(name, type.getName())) {
                return type;
            }
        }
        return null;
    }

    public VariableUserType getVariableUserTypeNotNull(String name) {
        VariableUserType type = getVariableUserType(name);
        if (type == null) {
            throw new InternalApplicationException("Type not found by name '" + name + "'");
        }
        return type;
    }

    @Override
    public ProcessDefinition makeCopy(GraphElement parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int hashCode() {
        if (hash32 != -1) {
            return hash32;
        }
        hash32 = Hashing.murmur3_32().hashString(file.getFullPath().toString()).asInt();
        return hash32;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || !(o instanceof ProcessDefinition)) {
            return false;
        }
        return hashCode() == o.hashCode();
    }

    public void addToVersionInfoList(VersionInfo versionInfo) {
        this.versionInfoList.add(versionInfo);
    }

    public ArrayList<VersionInfo> getVersionInfoList() {
        return versionInfoList;
    }

    public int getVersionInfoListIndex(VersionInfo versionInfo) {
        int result = -1;
        int i = 0;
        versionInfo = new VersionInfo(versionInfo.getDateTimeAsString(), versionInfo.getAuthor(), versionInfo.getComment().replaceAll("\r\n", "\n"));
        for (VersionInfo vi : this.getVersionInfoList()) {
            vi = new VersionInfo(vi.getDateTimeAsString(), vi.getAuthor(), vi.getComment().replaceAll("\r\n", "\n"));
            if (versionInfo.equals(vi)) {
                result = i;
                break;
            }
            i++;
        }
        return result;
    }

    public void setVersionInfoByIndex(int index, VersionInfo versionInfo) {
        versionInfoList.set(index, versionInfo);
    }

}

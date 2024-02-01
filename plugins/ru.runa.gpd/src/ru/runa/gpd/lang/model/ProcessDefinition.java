package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.regulations.RegulationsRegistry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventCapable;
import ru.runa.gpd.lang.model.bpmn.StartEventType;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.StartImagePropertyDescriptor;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.var.format.ListFormat;

@SuppressWarnings("unchecked")
public class ProcessDefinition extends NamedGraphElement implements Describable, GlobalObjectAware {
    protected Language language;
    protected NodeAsyncExecution defaultNodeAsyncExecution = NodeAsyncExecution.DEFAULT;
    protected boolean dirty;
    protected boolean showActions;
    protected boolean showGrid;
    protected Duration defaultTaskTimeoutDelay = new Duration();
    protected boolean invalid; // may be there is no concurrency but if any this is very important variable
    protected final AtomicInteger nextNodeIdCounter = new AtomicInteger();
    protected SwimlaneDisplayMode swimlaneDisplayMode = SwimlaneDisplayMode.none;
    protected final Map<String, SubprocessDefinition> embeddedSubprocesses = Maps.newHashMap();
    protected ProcessDefinitionAccessType accessType = ProcessDefinitionAccessType.Process;
    protected final List<VariableUserType> types = Lists.newArrayList();
    protected final IFile file;
    protected boolean usingGlobalVars;
    protected final Set<PropertyChangeListener> delegatedListeners = new HashSet<>();
    protected final ArrayList<VersionInfo> versionInfoList = new ArrayList<>();
    protected boolean regulationGenerated = false;
    private TaskStateExecutionButton executionButton = TaskStateExecutionButton.NONE;

    public ProcessDefinition(IFile file) {
        this.file = file;
    }

    public IFile getFile() {
        return file;
    }

    public ProcessDefinitionAccessType getAccessType() {
        return accessType;
    }

    public void addDelegatedListener(PropertyChangeListener delegatedListener) {
        this.delegatedListeners.add(delegatedListener);
    }

    public void removeDelegatedListener(PropertyChangeListener delegatedListener) {
        this.delegatedListeners.remove(delegatedListener);
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("composition".equals(name)) {
            return Objects.equal(value, String.valueOf(this instanceof SubprocessDefinition));
        }
        if ("hasExtendedRegulations".equals(name)) {
            return !RegulationsRegistry.hasExtendedRegulations();
        }
        if ("hasFormCSS".equals(name)) {
            try {
                IFile file = IOUtils.getAdjacentFile(getFile(), ParContentProvider.FORM_CSS_FILE_NAME);
                return Objects.equal(value, String.valueOf(file.exists()));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("testAttribute: hasFormCSS", e);
                return false;
            }
        }
        if ("hasFormJS".equals(name)) {
            try {
                IFile file = IOUtils.getAdjacentFile(getFile(), ParContentProvider.FORM_JS_FILE_NAME);
                return Objects.equal(value, String.valueOf(file.exists()));
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("testAttribute: hasFormJS", e);
                return false;
            }
        }
        if ("needGeneratedRegulation".equals(name)) {
            return Boolean.parseBoolean(value) ? this.regulationGenerated : true;
        }
        if ("needNotGeneratedRegulation".equals(name)) {
            return Boolean.parseBoolean(value) ? !this.regulationGenerated : true;
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
        }
    }

    public boolean isUsingGlobalVars() {
        return usingGlobalVars;
    }

    public void setUsingGlobalVars(boolean usingGlobalVars) {
        boolean stateChanged = this.usingGlobalVars != usingGlobalVars;
        if (stateChanged) {
            this.usingGlobalVars = usingGlobalVars;
            firePropertyChange(PROPERTY_USE_GLOBALS, !this.usingGlobalVars, this.usingGlobalVars);
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

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void onLoadingCompleted() {
        for (GraphElement graphElement : getChildrenRecursive(GraphElement.class)) {
            String nodeId = graphElement.getId();
            if (nodeId == null) {
                continue;
            }
            try {
                int id = Integer.parseInt(nodeId.substring(nodeId.lastIndexOf(".") + 1).substring(2));
                if (id > nextNodeIdCounter.get()) {
                    this.nextNodeIdCounter.set(id);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unable to parse node id " + nodeId);
            }
        }
    }

    public String getNextNodeId() {
        int nextId = this.nextNodeIdCounter.incrementAndGet();
        String nextNodeId = "ID" + nextId;
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
        } else if (startStates.size() > 1 && (!(this instanceof SubprocessDefinition) || !((SubprocessDefinition) this).isTriggeredByEvent())) {
            errors.add(ValidationError.createLocalizedError(this, "multipleStartStatesNotAllowed"));
        }
        for (Node unconnectedNode : findUnconnectedNodes()) {
            if (unconnectedNode.getArrivingTransitions().size() != 0) {
                errors.add(ValidationError.createLocalizedError(unconnectedNode, "unconnectedNodeIsPresent"));
                break;
            }
        }
        boolean invalid = false;
        for (ValidationError validationError : errors) {
            if (validationError.getSeverity() == IMarker.SEVERITY_ERROR) {
                invalid = true;
                break;
            }
        }
        if (this.invalid != invalid) {
            this.invalid = invalid;
            setDirty(true);
        }
    }

    public List<Node> findUnconnectedNodes() {
        Map<Node, Boolean> nodeIsConnectedMap = new HashMap<>();
        List<Node> startNodes = new ArrayList<>();
        for (Node node : getNodesRecursive()) {
            if (node instanceof IBoundaryEventCapable && ((IBoundaryEventCapable) node).isBoundaryEvent()) {
                startNodes.add(node);
            }
            nodeIsConnectedMap.put(node, false);
        }
        startNodes.addAll(getChildren(StartState.class));
        for (Node node : startNodes) {
            markNodeAsConnected(nodeIsConnectedMap, node);
        }
        return nodeIsConnectedMap.entrySet().stream().filter(e -> !e.getValue()).map(e -> e.getKey()).collect(Collectors.toList());
    }

    public void markNodeAsConnected(Map<Node, Boolean> nodeIsConnectedMap, Node node) {
        nodeIsConnectedMap.put(node, true);
        for (Transition transition : node.getLeavingTransitions()) {
            if (!nodeIsConnectedMap.get(transition.getTarget())) {
                markNodeAsConnected(nodeIsConnectedMap, transition.getTarget());
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
        List<Swimlane> swimlanes = getChildren(Swimlane.class);
        return swimlanes;
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
            if (Objects.equal(parentContainer, graphElement.getUiParentContainer())) {
                list.add(graphElement);
            }
            if (parentContainer == this && graphElement.getUiParentContainer() == null) {
                list.add(graphElement);
            }
        }
        return list;
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_TASKSTATE_EXECUTION_BUTTON,
                Localization.getString("property.customExecuteButtonName"), TaskStateExecutionButton.getLabels()));
        if (this instanceof SubprocessDefinition) {
            descriptors.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
            descriptors.add(new PropertyDescriptor(PROPERTY_TASK_DEADLINE, Localization.getString("default.task.deadline")));
            descriptors.add(new PropertyDescriptor(PROPERTY_ACCESS_TYPE, Localization.getString("ProcessDefinition.property.accessType")));
        } else {
            descriptors.add(new StartImagePropertyDescriptor("startProcessImage", Localization.getString("ProcessDefinition.property.startImage")));
            descriptors.add(new PropertyDescriptor(PROPERTY_LANGUAGE, Localization.getString("ProcessDefinition.property.language")));
            descriptors.add(new DurationPropertyDescriptor(PROPERTY_TASK_DEADLINE, this, getDefaultTaskTimeoutDelay(),
                    Localization.getString("default.task.deadline")));
            String[] array = { Localization.getString("ProcessDefinition.property.accessType.Process"),
                    Localization.getString("ProcessDefinition.property.accessType.OnlySubprocess") };
            descriptors.add(
                    new ComboBoxPropertyDescriptor(PROPERTY_ACCESS_TYPE, Localization.getString("ProcessDefinition.property.accessType"), array));
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_NODE_ASYNC_EXECUTION,
                    Localization.getString("ProcessDefinition.property.nodeAsyncExecution"), NodeAsyncExecution.LABELS));
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_USE_GLOBALS, Localization.getString("ProcessDefinition.property.useGlobals"),
                    new String[] { "false", "true" }));
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
        if (PROPERTY_USE_GLOBALS.equals(id)) {
            return usingGlobalVars ? 1 : 0;
        }
        if (PROPERTY_TASKSTATE_EXECUTION_BUTTON.equals(id)) {
            return executionButton.ordinal();
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
        } else if (PROPERTY_USE_GLOBALS.equals(id)) {
            setUsingGlobalVars(((int) value) == 1);
        } else if (PROPERTY_TASKSTATE_EXECUTION_BUTTON.equals(id)) {
            setExecutionButton(TaskStateExecutionButton.values()[(Integer) value]);
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

    public VariableUserType getTypeByName(String name) {
        if (name == null) {
            return null;
        }
        for (VariableUserType type : types) {
            if (name.equals(type.getName())) {
                return type;
            }
        }
        return null;
    }

    public VariableUserType getVariableUserType(String name) {
        for (VariableUserType type : getVariableUserTypes()) {
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
        return file.getFullPath().toString().hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof ProcessDefinition) {
            return file.equals(((ProcessDefinition) o).file);
        }
        return super.equals(o);
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

    public Swimlane getGlobalSwimlaneByName(String name) {
        if (name == null) {
            return null;
        }
        for (Swimlane swimlane : getChildren(Swimlane.class, s -> s.isGlobal())) {
            if (name.equals(swimlane.getName())) {
                return swimlane;
            }
        }
        return null;
    }

    public Variable getGlobalVariableByName(String name) {
        if (name == null) {
            return null;
        }
        for (Variable variable : getChildren(Variable.class, v -> v.isGlobal())) {
            if (name.equals(variable.getName())) {
                return variable;
            }
        }
        return null;
    }

    public VariableUserType getGlobalUserTypeByName(String name) {
        if (name == null) {
            return null;
        }
        for (VariableUserType type : types) {
            if (name.equals(type.getName())) {
                return type;
            }
        }
        return null;
    }

    public List<Swimlane> getGlobalSwimlanes() {
        return getChildren(Swimlane.class, s -> s.isGlobal());
    }

    public List<Variable> getGlobalVariables() {
        List<Variable> globalVariables = getChildren(Variable.class, v -> v.isGlobal());
        ;
        globalVariables.removeAll(getSwimlanes());
        return globalVariables;

    }

    public List<VariableUserType> getGlobalTypes() {
        return types.stream().filter(t -> t.isGlobal()).collect(Collectors.toList());
    }

    public void addGlobalSwimlane(Swimlane swimlane) {
        if (getSwimlaneByName(swimlane.getName()) == null) {
            addChild(swimlane);
        }
    }

    public void addGlobalVariable(Variable variable) {
        if (variable.isComplex()) {
            addGlobalType(variable.getUserType());
        }
        String variableFormat = variable.getFormatClassName();
        if (ListFormat.class.getName().equals(variableFormat)) {
            String typeName = variable.getFormat();
            int length = typeName.length();
            typeName = typeName.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length() + 1, length - 1).substring((variableFormat.length()));
            VariableUserType userType = ProcessCache.getGlobalProcessDefinition(this).getVariableUserType(typeName);
            if (userType != null) {
                userType.setName(IOUtils.GLOBAL_OBJECT_PREFIX + typeName);
                addGlobalType(userType);
            }
        }
        addChild(variable);

    }

    public void addGlobalType(VariableUserType type) {
        for (Variable variable : type.getAttributes()) {
            if (variable.isComplex()) {
                addGlobalType(variable.getUserType());
            }
        }
        VariableUserType typeToAdd = type.getCopy();
        if (!typeToAdd.getName().startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            typeToAdd.setName(IOUtils.GLOBAL_OBJECT_PREFIX + type.getName());
        }
        typeToAdd.setGlobal(true);
        if (!types.contains(typeToAdd)) {
            types.add(typeToAdd);
            firePropertyChange(PROPERTY_USER_TYPES_CHANGED, null, typeToAdd);
        }
    }

    public void updateGlobalObjects() {
        GlobalSectionDefinition globalDefinition = ProcessCache.getGlobalProcessDefinition(this);

        List<Swimlane> globalSwimlanes = getGlobalSwimlanes();
        for (Swimlane swimlane : globalSwimlanes) {
            Swimlane swimlaneFromGlobalSection = globalDefinition.getGlobalSwimlaneByName(swimlane.getName());
            if (swimlaneFromGlobalSection == null) {
                swimlane.setGlobal(false);
            } else {
                swimlane.updateFromGlobalPartition(swimlaneFromGlobalSection);
            }

        }
        // order of updating is important : we should update usertypes BEFORE usertype's variables
        for (VariableUserType userType : getGlobalTypes()) {
            VariableUserType userTypeFromGlobalPartition = globalDefinition.getGlobalUserTypeByName(userType.getName());
            if (userTypeFromGlobalPartition == null) {
                userType.setGlobal(false);
            } else {
                userType.updateFromGlobalPartition(userTypeFromGlobalPartition);
            }
        }

        List<Variable> globalVariables = getGlobalVariables();
        for (Variable variable : globalVariables) {
            Variable variableFromGlobalPartition = globalDefinition.getGlobalVariableByName(variable.getName());
            if (variableFromGlobalPartition == null) {
                variable.setGlobal(false);
            } else {
                variable.updateFromGlobalPartition(variableFromGlobalPartition);
            }
        }

    }

    public StartState getDefaultStartNode() {
        for (StartState startNode : getChildren(StartState.class)) {
            if (!startNode.isStartByEvent()) {
                return startNode;
            }
        }
        return null;
    }

    public void setDefaultStartNode(StartState theStartNode) {
        StartEventType oldEventType = StartEventType.signal;
        List<StartState> startNodes = getChildren(StartState.class);
        for (StartState startNode : startNodes) {
            if (startNode == theStartNode) {
                oldEventType = startNode.getEventType();
                startNode.setEventType(StartEventType.blank);
                break;
            }
        }
        Swimlane oldSwimlane = null;
        for (StartState startNode : startNodes) {
            if (startNode != theStartNode && !startNode.isStartByEvent()) {
                oldSwimlane = startNode.getSwimlane();
                startNode.setSwimlane(null);
                startNode.setEventType(oldEventType);
                break;
            }
        }
        if (oldSwimlane != null) {
            theStartNode.setSwimlane(oldSwimlane);
        }
    }

    public void setRegulationGenerated(boolean generated) {
        this.regulationGenerated = generated;
    }

    public boolean isRegulationGenerated() {
        return this.regulationGenerated;
    }

    public TaskStateExecutionButton getExecutionButton() {
        return executionButton;
    }

    public void setExecutionButton(TaskStateExecutionButton executionButton) {
        TaskStateExecutionButton old = this.executionButton;
        this.executionButton = executionButton;
        firePropertyChange(PROPERTY_TASKSTATE_EXECUTION_BUTTON, old, this.executionButton);
        for (Transition transition : this.getChildrenRecursive(Transition.class)) {
            transition.firePropertyChange(PROPERTY_TASKSTATE_EXECUTION_BUTTON, old, this.executionButton);
        }
    }
}

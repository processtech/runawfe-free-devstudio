package ru.runa.gpd.util;

import java.util.List;
import java.util.Map;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.swimlane.RelationSwimlaneInitializer;
import ru.runa.wfe.lang.MultiTaskCreationMode;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultiinstanceParameters {
    // backward compatibility
    private static final String USAGE_MULTIINSTANCE_VARS = "multiinstance-vars";
    private String discriminatorType = VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
    private String discriminatorVariableName = "";
    private String discriminatorGroup = "";
    private boolean discriminatorGroupInputAsText = false;
    private RelationSwimlaneInitializer discriminatorRelation = new RelationSwimlaneInitializer();
    private String iteratorVariableName = "";
    private MultiTaskCreationMode creationMode;
    private String swimlaneName;
    private String discriminatorCondition;

    public MultiinstanceParameters(List<VariableMapping> mappings) {
        discriminatorRelation.setInversed(true);
        for (VariableMapping mapping : mappings) {
            if (mapping.isMultiinstanceLinkByVariable()) {
                discriminatorType = VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
                discriminatorVariableName = mapping.getName();
                iteratorVariableName = mapping.getMappedName();
            }
            if (mapping.isMultiinstanceLinkByGroup()) {
                discriminatorType = VariableMapping.USAGE_DISCRIMINATOR_GROUP;
                discriminatorGroup = mapping.getName();
                discriminatorGroupInputAsText = mapping.isText();
                iteratorVariableName = mapping.getMappedName();
            }
            if (mapping.isMultiinstanceLinkByRelation()) {
                discriminatorType = VariableMapping.USAGE_DISCRIMINATOR_RELATION;
                try {
                    discriminatorRelation = new RelationSwimlaneInitializer(mapping.getName());
                } catch (Exception e) {
                    // PluginLogger.logErrorWithoutDialog(e.toString(), e);
                }
                if (!mapping.isText()) {
                    discriminatorRelation.setRelationName(VariableUtils.wrapVariableName(discriminatorRelation.getRelationName()));
                }
                iteratorVariableName = mapping.getMappedName();
            }
        }
    }

    public boolean isValid(boolean iteratorVariableRequired) {
        if (iteratorVariableRequired && Strings.isNullOrEmpty(iteratorVariableName)) {
            return false;
        }
        if (VariableMapping.USAGE_DISCRIMINATOR_VARIABLE.equals(discriminatorType)) {
            if (Strings.isNullOrEmpty(discriminatorVariableName)) {
                return false;
            }
        } else if (VariableMapping.USAGE_DISCRIMINATOR_GROUP.equals(discriminatorType)) {
            if (Strings.isNullOrEmpty(discriminatorGroup)) {
                return false;
            }
        } else if (VariableMapping.USAGE_DISCRIMINATOR_RELATION.equals(discriminatorType)) {
            return discriminatorRelation.isValid();

        } else {
            return false;
        }
        return true;
    }

    public String getDiscriminatorType() {
        return discriminatorType;
    }

    public void setDiscriminatorType(String type) {
        this.discriminatorType = type;
    }

    public String getDiscriminatorVariableName() {
        return discriminatorVariableName;
    }

    public void setDiscriminatorVariableName(String discriminatorVariableName) {
        this.discriminatorVariableName = discriminatorVariableName;
    }

    public String getDiscriminatorGroup() {
        return discriminatorGroup;
    }

    public void setDiscriminatorGroup(String discriminatorGroup) {
        this.discriminatorGroup = discriminatorGroup;
    }

    public boolean isDiscriminatorGroupInputAsText() {
        return discriminatorGroupInputAsText;
    }

    public void setDiscriminatorGroupInputAsText(boolean discriminatorGroupInputAsText) {
        this.discriminatorGroupInputAsText = discriminatorGroupInputAsText;
    }

    public RelationSwimlaneInitializer getDiscriminatorRelation() {
        return discriminatorRelation;
    }

    public String getIteratorVariableName() {
        return iteratorVariableName;
    }

    public void setIteratorVariableName(String iteratorVariableName) {
        this.iteratorVariableName = iteratorVariableName;
    }

    public MultiTaskCreationMode getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(MultiTaskCreationMode creationMode) {
        this.creationMode = creationMode;
    }

    public String getSwimlaneName() {
        return swimlaneName;
    }

    public void setSwimlaneName(String swimlaneName) {
        this.swimlaneName = swimlaneName;
    }

    public String getDiscriminatorCondition() {
        return discriminatorCondition;
    }

    public void setDiscriminatorCondition(String discriminatorCondition) {
        this.discriminatorCondition = discriminatorCondition;
    }

    public VariableMapping getDiscriminatorMapping() {
        if (VariableMapping.USAGE_DISCRIMINATOR_VARIABLE.equals(discriminatorType)) {
            String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
            return new VariableMapping(discriminatorVariableName, iteratorVariableName, usage);
        } else if (VariableMapping.USAGE_DISCRIMINATOR_GROUP.equals(discriminatorType)) {
            String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_GROUP;
            if (discriminatorGroupInputAsText) {
                usage += "," + VariableMapping.USAGE_TEXT;
            }
            return new VariableMapping(discriminatorGroup, iteratorVariableName, usage);
        } else if (VariableMapping.USAGE_DISCRIMINATOR_RELATION.equals(discriminatorType)) {
            String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_RELATION;
            if (VariableUtils.isVariableNameWrapped(discriminatorRelation.getRelationName())) {
                discriminatorRelation.setRelationName(VariableUtils.unwrapVariableName(discriminatorRelation.getRelationName()));
            } else {
                usage += "," + VariableMapping.USAGE_TEXT;
            }
            return new VariableMapping(discriminatorRelation.toString(), iteratorVariableName, usage);
        } else {
            throw new RuntimeException("Unexpected type value = " + discriminatorType);
        }
    }

    public void mergeTo(List<VariableMapping> mappings) {
        for (VariableMapping mapping : Lists.newArrayList(mappings)) {
            if (mapping.isMultiinstanceLink()
                    && (mapping.isMultiinstanceLinkByVariable() || mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation())) {
                mappings.remove(mapping);
            }
        }
        mappings.add(getDiscriminatorMapping());
    }

    public static List<VariableMapping> getCopyWithoutMultiinstanceLinks(List<VariableMapping> mappings) {
        List<VariableMapping> result = Lists.newArrayList(mappings);
        for (VariableMapping mapping : mappings) {
            if (mapping.isMultiinstanceLink()
                    && (mapping.isMultiinstanceLinkByVariable() || mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation())) {
                result.remove(mapping);
            }
        }
        return result;
    }

    private static int getConversionMode(List<VariableMapping> mappings) {
        boolean hasMultiinstanceLinks = false;
        for (VariableMapping mapping : mappings) {
            if (mapping.hasUsage(USAGE_MULTIINSTANCE_VARS)) {
                return 2;
            }
            if (mapping.isMultiinstanceLink()) {
                hasMultiinstanceLinks = true;
            }
            if (mapping.isMultiinstanceLinkByGroup() || mapping.isMultiinstanceLinkByRelation() || mapping.isMultiinstanceLinkByVariable()) {
                return 0;
            }
        }
        return hasMultiinstanceLinks ? 1 : 0;
    }

    public static void convertBackCompatible(MultiSubprocess multiSubprocess) {
        int conversionMode = getConversionMode(multiSubprocess.getVariableMappings());
        if (conversionMode == 0) {
            return;
        }
        try {
            List<VariableMapping> result = Lists.newArrayList(multiSubprocess.getVariableMappings());
            if (conversionMode == 2) {
                Map<String, VariableMapping> map = Maps.newHashMap();
                for (VariableMapping mapping : multiSubprocess.getVariableMappings()) {
                    if (mapping.hasUsage(USAGE_MULTIINSTANCE_VARS)) {
                        map.put(mapping.getName(), mapping);
                        result.remove(mapping);
                    }
                }
                VariableMapping typeMapping = map.get("typeMultiInstance");
                Preconditions.checkNotNull(typeMapping, "typeMultiInstance");
                if (VariableMapping.USAGE_DISCRIMINATOR_VARIABLE.equals(typeMapping.getMappedName())) {
                    String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
                    VariableMapping discriminatorMapping = map.get("tabVariableProcessVariable");
                    Preconditions.checkNotNull(discriminatorMapping, "tabVariableProcessVariable");
                    VariableMapping iteratorMapping = map.get("tabVariableSubProcessVariable");
                    Preconditions.checkNotNull(iteratorMapping, "tabVariableSubProcessVariable");
                    VariableMapping createdMapping = new VariableMapping(discriminatorMapping.getMappedName(), iteratorMapping.getMappedName(), usage);
                    result.add(createdMapping);
                    for (VariableMapping mapping : multiSubprocess.getVariableMappings()) {
                        if (mapping.isMultiinstanceLink() && mapping.isReadable() && !mapping.isWritable()
                                && mapping.getName().equals(createdMapping.getName())
                                && mapping.getMappedName().equals(createdMapping.getMappedName())) {
                            result.remove(mapping);
                        }
                    }
                } else if (VariableMapping.USAGE_DISCRIMINATOR_GROUP.equals(typeMapping.getMappedName())) {
                    String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_GROUP;
                    VariableMapping groupMapping = map.get("tabGroupName");
                    Preconditions.checkNotNull(groupMapping, "tabGroupName");
                    VariableMapping iteratorMapping = map.get("tabGroupSubProcessVariable");
                    Preconditions.checkNotNull(iteratorMapping, "tabGroupSubProcessVariable");
                    String discriminator;
                    if (VariableUtils.isVariableNameWrapped(groupMapping.getMappedName())) {
                        discriminator = VariableUtils.unwrapVariableName(groupMapping.getMappedName());
                    } else {
                        discriminator = groupMapping.getMappedName();
                        usage += "," + VariableMapping.USAGE_TEXT;
                    }
                    result.add(new VariableMapping(discriminator, iteratorMapping.getMappedName(), usage));
                } else if (VariableMapping.USAGE_DISCRIMINATOR_RELATION.equals(typeMapping.getMappedName())) {
                    String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_RELATION;
                    VariableMapping relationNameMapping = map.get("tabRelationName");
                    Preconditions.checkNotNull(relationNameMapping, "tabRelationName");
                    VariableMapping relationParamMapping = map.get("tabRelationParam");
                    Preconditions.checkNotNull(relationParamMapping, "tabRelationParam");
                    VariableMapping iteratorMapping = map.get("tabRelationSubProcessVariable");
                    Preconditions.checkNotNull(iteratorMapping, "tabRelationSubProcessVariable");
                    RelationSwimlaneInitializer initializer = new RelationSwimlaneInitializer();
                    initializer.setInversed(true);
                    if (VariableUtils.isVariableNameWrapped(relationNameMapping.getMappedName())) {
                        initializer.setRelationName(VariableUtils.unwrapVariableName(relationNameMapping.getMappedName()));
                    } else {
                        initializer.setRelationName(relationNameMapping.getMappedName());
                        usage += "," + VariableMapping.USAGE_TEXT;
                    }
                    if (!VariableUtils.isVariableNameWrapped(relationParamMapping.getMappedName())) {
                        throw new RuntimeException("tabRelationParam text value is not supported since v4.1.1. Use variable default value for this.");
                    }
                    initializer.setRelationParameterVariableName(VariableUtils.unwrapVariableName(relationParamMapping.getMappedName()));
                    result.add(new VariableMapping(initializer.toString(), iteratorMapping.getMappedName(), usage));
                } else {
                    throw new RuntimeException("typeMultiInstance: unknown value = " + typeMapping.getMappedName());
                }
            } else {
                for (VariableMapping mapping : multiSubprocess.getVariableMappings()) {
                    if (mapping.isMultiinstanceLink() && mapping.isReadable() && !mapping.isWritable()) {
                        result.remove(mapping);
                        String usage = VariableMapping.USAGE_MULTIINSTANCE_LINK + "," + VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
                        result.add(new VariableMapping(mapping.getName(), mapping.getMappedName(), usage));
                        break;
                    }
                }
            }
            multiSubprocess.setVariableMappings(result);
        } catch (Exception e) {
            PluginLogger.logError(
                    Localization.getString("MultiSubprocess.ConversionFailed", multiSubprocess.getProcessDefinition().getName(), multiSubprocess), e);
        }
    }
}

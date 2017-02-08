package ru.runa.gpd.util;

import com.google.common.base.Objects;

public class VariableMapping {
    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_SYNC = "sync";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_SELECTOR = "selector";
    public static final String USAGE_TEXT = "text";
    public static final String USAGE_DISCRIMINATOR_VARIABLE = "variable";
    public static final String USAGE_DISCRIMINATOR_GROUP = "group";
    public static final String USAGE_DISCRIMINATOR_RELATION = "relation";

    private String name;
    private String mappedName;
    private String usage;

    public VariableMapping() {
    }

    public VariableMapping(String name, String mappedName, String usage) {
        this.name = name;
        this.mappedName = mappedName;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String subprocessVariable) {
        this.mappedName = subprocessVariable;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isReadable() {
        return hasUsage(USAGE_READ);
    }

    public boolean isWritable() {
        return hasUsage(USAGE_WRITE);
    }

    public boolean isSyncable() {
        return hasUsage(USAGE_SYNC);
    }

    public boolean isMultiinstanceLink() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK);
    }

    public boolean isMultiinstanceLinkByVariable() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_VARIABLE);
    }

    public boolean isMultiinstanceLinkByGroup() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_GROUP);
    }

    public boolean isMultiinstanceLinkByRelation() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_RELATION);
    }

    public boolean isPropertySelector() {
        return hasUsage(USAGE_SELECTOR);
    }

    public boolean isText() {
        return hasUsage(USAGE_TEXT);
    }

    public boolean hasUsage(String accessLiteral) {
        return hasUsage(usage, accessLiteral);
    }

    public static boolean hasUsage(String usage, String accessLiteral) {
        if (usage == null) {
            return false;
        }
        return usage.indexOf(accessLiteral) != -1;
    }

    public VariableMapping getCopy() {
        return new VariableMapping(name, mappedName, usage);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableMapping) {
            VariableMapping m = (VariableMapping) obj;
            return Objects.equal(name, m.name) && Objects.equal(mappedName, m.mappedName) && Objects.equal(usage, m.usage);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, mappedName, usage);
    }

    @Override
    public String toString() {
        return name + "=" + mappedName + " (" + usage + ")";
    }
}

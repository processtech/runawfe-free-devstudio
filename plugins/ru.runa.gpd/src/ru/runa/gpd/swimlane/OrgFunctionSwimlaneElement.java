package ru.runa.gpd.swimlane;

import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;

import com.google.common.base.Objects;

public abstract class OrgFunctionSwimlaneElement extends SwimlaneElement<OrgFunctionSwimlaneInitializer> {
    private String orgFunctionDefinitionName;

    public OrgFunctionSwimlaneElement(String orgFunctionDefinitionName) {
        setOrgFunctionDefinitionName(orgFunctionDefinitionName);
    }

    public void setOrgFunctionDefinitionName(String orgFunctionDefinitionName) {
        this.orgFunctionDefinitionName = orgFunctionDefinitionName;
    }

    @Override
    protected OrgFunctionSwimlaneInitializer createNewSwimlaneInitializer() {
        OrgFunctionDefinition orgFunctionDefinition = OrgFunctionsRegistry.getInstance().getArtifactNotNull(orgFunctionDefinitionName);
        return new OrgFunctionSwimlaneInitializer(orgFunctionDefinition);
    }

    @Override
    protected boolean isSwimlaneInitializerSuitable(OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        if (swimlaneInitializer == null) {
            return true;
        }
        OrgFunctionDefinition orgFunctionDefinition = OrgFunctionsRegistry.getInstance().getArtifactNotNull(orgFunctionDefinitionName);
        return Objects.equal(swimlaneInitializer.getDefinition(), orgFunctionDefinition);
    }

    protected String getOrgFunctionParameterValue(int index) {
        return getSwimlaneInitializerNotNull().getParameters().get(index).getValue();
    }

    protected void setOrgFunctionParameterValue(int index, String value) {
        getSwimlaneInitializerNotNull().getParameters().get(index).setValue(value);
    }

    @Override
    protected void fireCompletedEvent() {
        boolean fireEvent = true;
        for (OrgFunctionParameter parameter : getSwimlaneInitializerNotNull().getParameters()) {
            if (parameter.getValue().length() == 0) {
                fireEvent = false;
            }
        }
        if (fireEvent) {
            super.fireCompletedEvent();
        }
    }
}

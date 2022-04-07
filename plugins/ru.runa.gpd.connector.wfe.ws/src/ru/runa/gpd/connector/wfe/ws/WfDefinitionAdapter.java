package ru.runa.gpd.connector.wfe.ws;

import java.util.List;

import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.user.Actor;

import com.google.common.collect.Lists;

public class WfDefinitionAdapter {

    public static WfDefinition toDTO(ru.runa.wfe.webservice.WfDefinition definition) {
        Deployment deployment = new Deployment();
        deployment.setCategories(definition.getCategories());
        deployment.setCreateDate(DateAdapter.toDTO(definition.getCreateDate()));
        deployment.setCreateActor(actorToDTO(definition.getCreateActor()));
        deployment.setUpdateDate(DateAdapter.toDTO(definition.getUpdateDate()));
        deployment.setUpdateActor(actorToDTO(definition.getUpdateActor()));
        deployment.setDescription(definition.getDescription());
        deployment.setId(definition.getId());
        deployment.setName(definition.getName());
        deployment.setVersion(definition.getVersion());
        return new WfDefinition(deployment);
    }

    public static List<WfDefinition> toDTOs(List<ru.runa.wfe.webservice.WfDefinition> definitions) {
        List<WfDefinition> result = Lists.newArrayListWithExpectedSize(definitions.size());
        for (ru.runa.wfe.webservice.WfDefinition definition : definitions) {
            result.add(toDTO(definition));
        }
        return result;
    }

	private static Actor actorToDTO(ru.runa.wfe.webservice.Actor wsActor) {
		return wsActor == null ? null : new Actor(wsActor.getName(), "");
	}
}

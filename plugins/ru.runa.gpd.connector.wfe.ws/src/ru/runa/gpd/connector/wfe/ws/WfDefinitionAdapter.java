package ru.runa.gpd.connector.wfe.ws;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.user.Actor;

public class WfDefinitionAdapter {

    public static WfDefinition toDTO(ru.runa.wfe.webservice.WfDefinition definition) {
    	ProcessDefinition d = new ProcessDefinition();
    	ProcessDefinitionVersion dv = new ProcessDefinitionVersion();
        d.setId(definition.getId());
        d.setName(definition.getName());
        d.setDescription(definition.getDescription());
        d.setCategories(definition.getCategories());
        dv.setDefinition(d);
        dv.setId(definition.getVersionId());
        dv.setCreateDate(DateAdapter.toDTO(definition.getCreateDate()));
        dv.setCreateActor(actorToDTO(definition.getCreateActor()));
        dv.setUpdateDate(DateAdapter.toDTO(definition.getUpdateDate()));
        dv.setUpdateActor(actorToDTO(definition.getUpdateActor()));
        dv.setVersion(definition.getVersion());
        return new WfDefinition(d, dv);
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

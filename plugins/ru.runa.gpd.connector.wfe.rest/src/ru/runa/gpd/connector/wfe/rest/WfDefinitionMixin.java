package ru.runa.gpd.connector.wfe.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.runa.wfe.user.Actor;

public abstract class WfDefinitionMixin {

    @JsonProperty("createUser")
    Actor createActor;

    @JsonProperty("updateUser")
    Actor updateActor;

    @JsonCreator
    public WfDefinitionMixin(
            @JsonProperty("createUser") Actor createActor,
            @JsonProperty("updateUser") Actor updateActor) {
    }
}

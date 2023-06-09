package ru.runa.gpd.connector.wfe.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfePagedList;

public class Mapper {
    private ObjectMapper mapper;

    public Mapper() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(WfDefinition.class, WfDefinitionMixin.class);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE).withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public <T> T toObject(byte[] data, Class<T> clazz) {
        try {
            return mapper.readValue(new String(data), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> toList(byte[] data, Class<T> clazz) {
        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(List.class, clazz);
            return mapper.readValue(new String(data), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> WfePagedList<T> toWfeList(byte[] data, Class<T> clazz) {
        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(WfePagedList.class, clazz);
            return mapper.readValue(new String(data), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> convertList(List<?> list, Class<T> clazz) {
        return list.stream().map(obj -> mapper.convertValue(obj, clazz)).collect(Collectors.toList());
    }

    public HttpEntity toHttpEntity(Object body) {
        try {
            String json = mapper.writeValueAsString(body);
            StringEntity out = new StringEntity(json, ContentType.APPLICATION_JSON);
            return out;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpEntity toHttpEntity(String fileName, byte[] file) {
        return new ByteArrayEntity(file);
    }
    
    public Map<String, Boolean> convertExecutors(WfePagedList<WfeExecutor> executors) {
        Map<String, Boolean> executorIsGroupMap = new HashMap<>();
        for(WfeExecutor executor : executors.getData()) {
            boolean isGroup = executor.getType() == WfeExecutor.Type.GROUP || executor.getType() == WfeExecutor.Type.DELEGATION_GROUP
                    || executor.getType() == WfeExecutor.Type.ESCALATION_GROUP || executor.getType() == WfeExecutor.Type.TEMPORARY_GROUP;
            executorIsGroupMap.put(executor.getName(), isGroup);
        }
        return executorIsGroupMap;
    }

}

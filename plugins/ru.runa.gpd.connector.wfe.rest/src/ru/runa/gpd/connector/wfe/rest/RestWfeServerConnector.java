package ru.runa.gpd.connector.wfe.rest;

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.rest.dto.WfeCredentials;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeProcessDefinition;
import ru.runa.wfe.rest.dto.WfeRelation;

public class RestWfeServerConnector extends WfeServerConnector {
    private static final Mapper mapper = new Mapper();
    private ApiClient client;

    @Override
    public void connect() {
        client = new ApiClient(settings.getProtocol(), settings.getHost(), settings.getPort());
        if (AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(settings.getAuthenticationType())) {
            WfeCredentials credentials = new WfeCredentials();
            credentials.setLogin(settings.getLogin());
            credentials.setPassword(settings.getPassword());
            URI uri = client.buildURI("/auth/basic");
            HttpEntity entity = mapper.toHttpEntity(credentials);
            byte[] response = client.post(uri, entity);
            client.setToken(new String(response));
        } else {
            URI uri = client.buildURI("/auth/kerberos");
            HttpEntity entity = mapper.toHttpEntity("token", getKerberosToken());
            byte[] response = client.post(uri, entity);
            client.setToken(new String(response));
        }
    }

    @Override
    public void disconnect() throws Exception {
        client = null;
    }

    @Override
    public Map<String, Boolean> getExecutors() {
        URI uri = client.buildURI("/executor/list");
        HttpEntity entity = mapper.toHttpEntity(new WfePagedListFilter());
        byte[] response = client.post(uri, entity);
        WfePagedList<WfeExecutor> executors = mapper.toWfeList(response, WfeExecutor.class);
        return mapper.convertExecutors(executors);
    }

    @Override
    public List<String> getRelationNames() {
        URI uri = client.buildURI("/relation/list");
        HttpEntity entity = mapper.toHttpEntity(new WfePagedListFilter());
        byte[] response = client.post(uri, entity);
        WfePagedList<WfeRelation> relations = mapper.toWfeList(response, WfeRelation.class);
        return relations.getData().stream().map(WfeRelation::getName).collect(Collectors.toList());
    }

    @Override
    public List<WfDefinition> getProcessDefinitions() {
        URI uri = client.buildURI("/definition/list");
        HttpEntity entity = mapper.toHttpEntity(new WfePagedListFilter());
        byte[] response = client.post(uri, entity);
        WfePagedList<WfeProcessDefinition> definitions = mapper.toWfeList(response, WfeProcessDefinition.class);
        return mapper.convertList(definitions.getData(), WfDefinition.class);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(WfDefinition definition) {
        URI uri = client.buildURI("/definition/history", new BasicNameValuePair("name", definition.getName()));
        byte[] response = client.get(uri);
        List<WfeProcessDefinition> definitions = mapper.toList(response, WfeProcessDefinition.class);
        return mapper.convertList(definitions, WfDefinition.class);
    }

    @Override
    public byte[] getProcessDefinitionArchive(WfDefinition definition) {
        URI uri = client.buildURI("/definition/" + definition.getId() + "/file", new BasicNameValuePair("name", "par"));
        return client.get(uri);
    }

    @Override
    public WfDefinition deployProcessDefinitionArchive(byte[] par) {
        URI uri = client.buildURI("/definition/", new BasicNameValuePair("categories", ""));
        HttpEntity entity = mapper.toHttpEntity("par", par);
        byte[] response = client.put(uri, entity);
        return mapper.toObject(response, WfDefinition.class);
    }

    @Override
    public WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types) {
        URI uri = client.buildURI("/definition/" + definitionId + "/redeploy", new BasicNameValuePair("categories", String.join(",", types)));
        HttpEntity entity = mapper.toHttpEntity("par", par);
        byte[] response = client.patch(uri, entity);
        return mapper.toObject(response, WfDefinition.class);
    }

    @Override
    public WfDefinition updateProcessDefinitionArchive(Long definitionId, byte[] par) {
        URI uri = client.buildURI("/definition/" + definitionId + "/update");
        HttpEntity entity = mapper.toHttpEntity("par", par);
        byte[] response = client.patch(uri, entity);
        return mapper.toObject(response, WfDefinition.class);
    }

    @Override
    public Map<Bot, List<BotTask>> getBots() {
        URI uri = client.buildURI("/bot/list");
        byte[] response = client.get(uri);
        List<Bot> bots = mapper.toList(response, Bot.class);
        Map<Bot, List<BotTask>> result = Maps.newHashMap();
        for (Bot bot : bots) {
            uri = client.buildURI("/bot/" + bot.getId() + "/tasks");
            result.put(bot, mapper.toList(client.get(uri), BotTask.class));
        }
        return result;
    }

    @Override
    public byte[] getBotFile(Bot bot) {
        URI uri = client.buildURI("/bot/export");
        HttpEntity entity = mapper.toHttpEntity(bot);
        return client.post(uri, entity);
    }

    @Override
    public byte[] getBotTaskFile(Bot bot, String botTask) {
        URI uri = client.buildURI("/bot/task/export", new BasicNameValuePair("botTaskName", botTask));
        HttpEntity entity = mapper.toHttpEntity(bot);
        return client.post(uri, entity);
    }

    @Override
    public void deployBot(String botStationName, byte[] archive) {
        URI uri = client.buildURI("/bot/import", new BasicNameValuePair("replace", "true"), new BasicNameValuePair("botStationName", botStationName));
        HttpEntity entity = mapper.toHttpEntity("archive", archive);
        client.put(uri, entity);
    }

    @Override
    public byte[] getBotStationFile(BotStation botStation) {
        URI uri = client.buildURI("/bot/station/export");
        HttpEntity entity = mapper.toHttpEntity(botStation);
        return client.post(uri, entity);
    }

    @Override
    public void deployBotStation(byte[] archive) {
        URI uri = client.buildURI("/bot/station/import", new BasicNameValuePair("replace", "true"));
        HttpEntity entity = mapper.toHttpEntity("archive", archive);
        client.put(uri, entity);
    }

    @Override
    public List<BotStation> getBotStations() {
        URI uri = client.buildURI("/bot/station/list");
        byte[] response = client.get(uri);
        return mapper.toList(response, BotStation.class);
    }

    @Override
    public void deployDataSourceArchive(byte[] archive) {
        URI uri = client.buildURI("/datasource/");
        HttpEntity entity = mapper.toHttpEntity("archive", archive);
        client.put(uri, entity);
    }

    @Override
    public byte[] getDataSourceArchive(String dsName) {
        URI uri = client.buildURI("/datasource/export", new BasicNameValuePair("name", dsName));
        return client.post(uri, null);
    }

    @Override
    public List<String> getDataSourceNames() {
        URI uri = client.buildURI("/datasource/names");
        byte[] response = client.get(uri);
        return mapper.toList(response, String.class);
    }

}

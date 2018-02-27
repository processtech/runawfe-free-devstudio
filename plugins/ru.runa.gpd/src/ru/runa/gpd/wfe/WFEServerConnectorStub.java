package ru.runa.gpd.wfe;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WFEServerConnectorStub extends WFEServerConnector {
    @Override
    public void connect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Boolean> getExecutors() {
        return Maps.newHashMap();
    }

    @Override
    public List<String> getRelationNames() {
        return Lists.newArrayList();
    }

    @Override
    public Map<WfDefinition, List<WfDefinition>> getProcessDefinitions(IProgressMonitor monitor) {
        return Maps.newHashMap();
    }

    @Override
    public byte[] getProcessDefinitionArchive(WfDefinition definition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WfDefinition deployProcessDefinitionArchive(byte[] par) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Bot, List<BotTask>> getBots() {
        return Maps.newHashMap();
    }

    @Override
    public byte[] getBotFile(Bot bot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBotTaskFile(Bot bot, String botTask) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deployBot(String botStationName, byte[] archive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBotStationFile(BotStation botStation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deployBotStation(byte[] archive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BotStation> getBotStations() {
        return Lists.newArrayList();
    }

    @Override
    public void deployDataSourceArchive(byte[] archive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getDataSourceArchive(String dsName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getDataSourceNames() {
        throw new UnsupportedOperationException();
    }

}

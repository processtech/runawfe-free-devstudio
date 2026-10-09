package ru.runa.gpd.aichat.api;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Optional;

public enum BpmnCommand {
    ADD_EVENT("add_event", 3) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.addEvent(params);
        }
    },
    ADD_ACTIVITY("add_activity", 4) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.addActivity(params);
        }
    },
    ADD_GATEWAY("add_gateway", 5) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.addGateway(params);
        }
    },
    ADD_VARIABLE("add_variable", 6) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.addVariable(params);
        }
    },
    DELETE_ELEMENT("delete_element", 1) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.deleteElement(params);
        }
    },
    DELETE_CONNECT("delete_connect", 2) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.deleteConnect(params);
        }
    },
    CONNECT("connect", 7) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params)  throws Exception  {
            bpmnBuilder.connect(params);
        }
    },
    ADD_LANE("add_lane", 8) {
        @Override
        public void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception {
            bpmnBuilder.addLane(params);
        }
    };

    private final String commandName;
    private final int executeOrder;

    BpmnCommand(String commandName, int executeOrder) {
        this.commandName = commandName;
        this.executeOrder = executeOrder;
    }

    public String getCommandName() {
        return commandName;
    }
    public int getExecuteOrder() {
        return executeOrder;
    }

    public abstract void execute(IBpmnBuilder bpmnBuilder, JsonNode params) throws Exception;

    public static Optional<BpmnCommand> fromString(String text) {
        return Arrays.stream(values())
                .filter(command -> command.commandName.equalsIgnoreCase(text))
                .findFirst();
    }
}

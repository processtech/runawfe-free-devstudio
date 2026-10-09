package ru.runa.gpd.aichat.action;

import ru.runa.gpd.aichat.view.AiChatView;
import ru.runa.gpd.ui.action.OpenViewBaseAction;

public class OpenAiChat extends OpenViewBaseAction {

    @Override
    protected String getViewId() {
        return AiChatView.VIEW_ID;
    }

}
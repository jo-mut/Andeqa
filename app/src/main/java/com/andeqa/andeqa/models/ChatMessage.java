package com.andeqa.andeqa.models;

import android.app.LauncherActivity;

public class ChatMessage extends Chat {
    private Chat chat;

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public int getType() {
        return 0;
    }
}

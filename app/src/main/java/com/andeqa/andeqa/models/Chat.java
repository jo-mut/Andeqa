package com.andeqa.andeqa.models;

public abstract class Chat {
    public static final int TYPE_PERIOD = 0;
    public static final int TYPE_CHAT = 1;
    abstract public int getType();
}

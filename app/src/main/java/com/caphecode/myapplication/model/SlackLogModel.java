package com.caphecode.myapplication.model;


public class SlackLogModel {
    private String text;

    public SlackLogModel(String tag, String messages) {
        this.text = tag + "--------\n" + messages;
    }
}
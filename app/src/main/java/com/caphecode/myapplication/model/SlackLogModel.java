package com.caphecode.myapplication.model;


public class SlackLogModel {
    private String text;

    public SlackLogModel(String coin, String from, String to, String total ) {
        this.text = String.format("{\n" +
                "  \"blocks\": [\n" +
                "    {\n" +
                "      \"type\": \"section\",\n" +
                "      \"text\": {\n" +
                "        \"type\": \"mrkdwn\",\n" +
                "        \"text\": \"*From* `%s` *To* `%s` *%s* `%s`\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}", from, to, coin, to, total);
    }
}
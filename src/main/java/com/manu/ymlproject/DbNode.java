package com.manu.ymlproject;

import com.fasterxml.jackson.databind.JsonNode;

public class DbNode {
    public final String name;
    public final String value;

    public DbNode(String name, JsonNode value) {
        this.name = name;
        this.value = value.asText();
    }
}

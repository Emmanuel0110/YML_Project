package com.manu.ymlproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbNode {
    public final String name;
    public final String value;

    public DbNode(String name, JsonNode value) {
        this.name = name;
        this.value = value.toString();
    }
}

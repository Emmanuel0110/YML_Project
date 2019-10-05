package com.manu.ymlproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Node {
    public final String name;
    public final String value;
    public final String path;
//    public final String type;

    public Node(String name, JsonNode value, String path) throws Exception {
        this.name = name;
        this.value = value.toString();
        this.path = path;
//        if (value instanceof IntNode) {
//            this.type = "int";
//        } else if (value instanceof TextNode) {
//            Pattern p = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
//            Matcher m = p.matcher(value.toString());
//            if (m.find()){
//                this.type = "date";
//            } else {
//                this.type = "string";
//            }
//        } else if (value instanceof DoubleNode) {
//            this.type = "double";
//        } else if (value instanceof NullNode) {
//            this.type = "null";
//        } else {
//            throw new Exception("Unknown type : " + value.getClass().getName());
//        }
    }
}

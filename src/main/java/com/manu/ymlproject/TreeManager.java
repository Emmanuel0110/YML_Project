package com.manu.ymlproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.ArrayList;
import java.lang.String;

//public class TreeManager implements CommandLineRunner {
public class TreeManager {
    private final Database database;
    final String path;

    public TreeManager(Database database, String path) {
        this.database = database;
        this.path = path;
    }

//    @Override
//    public void run(String... args) throws Exception {
//        final var mapper = new ObjectMapper(new YAMLFactory());
//        final var tree = mapper.readTree(new File("info.yaml"));
//        readNode(tree);
//        var somePaths = getPath("implants:probe", "\"CambridgeParallel_16x2\""); //returns ["C:\MyFile"]
//        var anEmptyArray = getPath("implants:probe", "\"CambridgeParallel_16x3\""); //returns []
//        System.out.println(somePaths + "\n" + anEmptyArray);
//        System.out.println(database.names());
//    }

    void readNode(JsonNode tree){
        readNode(tree, "");
    }

    private void readNode(JsonNode tree, String previousKey) {
        var keys = tree.fieldNames();
        while (keys.hasNext()) {
            var nextKey = keys.next();
            var value = tree.get(nextKey);
            if (!previousKey.equals("")) {
                nextKey = previousKey + ":" + nextKey;
            }
            readValue(value, nextKey);
        }
    }

    private void readValue(JsonNode value, String name){ //inverser paramètres
        if (value instanceof ObjectNode) {
            readNode(value, name);
        } else if (value instanceof ArrayNode) {
            for (var subValue : value) {
                readValue(subValue, name);
            }
        } else {
//            System.out.println(name + " " + value + " " + value.getClass().getName());
            try {
                var newNode = new Node(name, value, path);
                this.database.save(newNode);
            } catch (Exception e){
                System.out.println("error : " + e.getMessage());
            }
        }
    }

    private ArrayList<String> getPath(String name, String value){
        return database.paths(name, value);
    }
}

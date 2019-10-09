package com.manu.ymlproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.String;
import java.util.List;

@Component
public class YamlService {
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    Yaml yaml(MultipartFile file, String filePath) throws IOException {
            final var tree = mapper.readTree(file.getInputStream());
            var nodes = dbNodes(tree, new ArrayList<>());;
            return new Yaml(filePath, nodes);
    }

    public List<DbNode> dbNodes(JsonNode node, List<String> nodePath){ //inverser paramètres
        if (node instanceof ObjectNode) {
            return dbNodesFromObject(node, nodePath);
        } else if (node instanceof ArrayNode) {
            return dbNodesFromArray(node, nodePath);
        } else {
            return List.of(new DbNode(String.join(":", nodePath), node));
        }
    }

    private List<DbNode> dbNodesFromObject(JsonNode objectNode, List<String> objectPath){
        var fieldNames = objectNode.fieldNames();
        var dbNodes = new ArrayList<DbNode>();
        while (fieldNames.hasNext()) {
            var fieldName = fieldNames.next();
            var node = objectNode.get(fieldName);
            var nodePath = new ArrayList<>(objectPath);
            nodePath.add(fieldName);
            dbNodes.addAll(dbNodes(node,nodePath));
        }
        return dbNodes;
    }

    private List<DbNode> dbNodesFromArray(JsonNode arrayNode, List<String> arrayPath){
        var nodes = new ArrayList<DbNode>();
        for (var node : arrayNode) {
            nodes.addAll(dbNodes(node, arrayPath));
        }
        return nodes;
    }
}

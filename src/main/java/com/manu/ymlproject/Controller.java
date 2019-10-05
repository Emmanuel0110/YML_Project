package com.manu.ymlproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;

@RestController
public class Controller {
    private final Database database;

    public Controller(Database database) {
        this.database = database;
    }
    @GetMapping("names")
    ArrayList<String> getNames(){
        return database.names();
    }

    @GetMapping("node")
    ArrayList<String> getPaths(@RequestParam String name, @RequestParam String value){
        return database.paths(name, value);
    }

    @PostMapping("file")
    void handleFileUpload(@RequestParam MultipartFile file, @RequestParam String path) throws IOException {
        final var mapper = new ObjectMapper(new YAMLFactory());
        final var tree = mapper.readTree(file.getInputStream());
        final var treeManager = new TreeManager(database, path);
        treeManager.readNode(tree);
    }
}

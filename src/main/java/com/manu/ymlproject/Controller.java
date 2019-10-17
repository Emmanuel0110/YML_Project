package com.manu.ymlproject;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;

@RestController
public class Controller {
    private final Database database;
    private final YamlService yamlService;

    public Controller(Database database, YamlService yamlService) {
        this.database = database;
        this.yamlService = yamlService;
    }

    @GetMapping("names")
    ArrayList<String> names(){
        return database.names();
    }

//    @GetMapping("node")
@PostMapping("node")
//    ArrayList<String> getPaths(@RequestParam String name, @RequestParam String value){
    ArrayList<String> getPaths(@RequestBody QueryForm[] queryForms){
        return database.paths(queryForms);
    }

    @PostMapping("file")
    void saveFile(@RequestParam MultipartFile file, @RequestParam("path") String filePath) throws BadRequestException {
        try {
            var yaml = yamlService.yaml(file, filePath);
            database.save(yaml);
        } catch (IOException e) {
            throw new BadRequestException("Input stream couldn't be read");
        }
    }
}

package com.manu.ymlproject;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

@RestController
public class Controller {
    private final Database database;
    private final YamlService yamlService;

    public Controller(Database database, YamlService yamlService) {
        this.database = database;
        this.yamlService = yamlService;
    }

    @PostMapping("file")
    void saveFile(@RequestParam MultipartFile file, @RequestParam("path") String filePath) throws BadRequestException {
        if (filePath == "") throw new BadRequestException("Path is missing");
        try {
            var yaml = yamlService.yaml(file, filePath);
            database.save(yaml);
        } catch (IOException e) {
            throw new BadRequestException("Input stream couldn't be read");
        }
    }

    @GetMapping("attributes")
    ArrayList<Attribute> attributes(){ return database.attributes(); }

    @PostMapping("paths")
    ArrayList<String> getPaths(@RequestBody QueryForm[] queryForms) throws BadRequestException {
        if (isNotValid(queryForms)) throw new BadRequestException("Inputs are not valid");
        try {
            return database.paths(queryForms);
        } catch (DateTimeParseException e){
            throw new BadRequestException("Invalid date format. Please use dd-mm-yyyy format");
        }
    }

    private boolean isNotValid(QueryForm[] queryForms) {
        if (queryForms.length == 0) return true;

        for (QueryForm queryForm : queryForms){
            if (queryForm.name.isEmpty() || queryForm.operator.isEmpty() || queryForm.value.isEmpty()) return true;
        }

        return false;
    }
}

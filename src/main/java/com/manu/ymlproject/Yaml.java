package com.manu.ymlproject;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Yaml {

    public Integer id;
    public final List<DbNode> dbNodes;
    public final String filePath;

    public Yaml(String filePath, List<DbNode> dbNodes){
        this.filePath = filePath;
        this.dbNodes = dbNodes;
    }
}

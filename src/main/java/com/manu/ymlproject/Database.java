package com.manu.ymlproject;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Component
public class Database {
    private final DataSource dataSource;

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void save(Yaml yaml) throws RuntimeException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try(var statement = connection.prepareStatement( "INSERT INTO tree(filePath) VALUES(?);", RETURN_GENERATED_KEYS)) {
                statement.setString(1, yaml.filePath);
                statement.executeUpdate();
                var rs = statement.getGeneratedKeys();
                rs.next();
                yaml.id = rs.getInt(1);
            }
            try (var statement = connection.prepareStatement( "INSERT INTO node(name,value,tree_id) VALUES(?,?,?);")) {
                for (var node : yaml.dbNodes) {
                    statement.setString(1, node.name);
                    statement.setString(2, node.value);
                    statement.setInt(3, yaml.id);
                    statement.executeUpdate();
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<String> names(){
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("SELECT DISTINCT name FROM node ORDER BY name");
        ) {
            ResultSet rs = statement.executeQuery();
            ArrayList<String> arrStr = new ArrayList<>();
            while (rs.next()) {
                arrStr.add(rs.getString("name"));
            }
            return arrStr;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<String> paths(String nodeName, String nodeValue){
        try (
                var connection = dataSource.getConnection();
                    var statement = connection.prepareStatement("SELECT DISTINCT tree.filePath as path " +
                            "FROM node JOIN tree " +
                            "ON node.tree_id= tree.id " +
                            "WHERE node.name = ? AND node.value = ?");
        ) {
            statement.setString(1, nodeName);
            statement.setString(2, nodeValue);
            ResultSet rs = statement.executeQuery();
            ArrayList<String> arrStr = new ArrayList<>();
            while (rs.next()) {
                arrStr.add(rs.getString("path"));
            }
            return arrStr;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}


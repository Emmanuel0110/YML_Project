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

//    List<Person> allPersons() {
//        try (
//                var connection = dataSource.getConnection();
//                var statement = connection.prepareStatement("SELECT id, name, age FROM city")
//        ) {
//            var rs = statement.executeQuery();
//            var persons = new ArrayList<Person>();
//            while (rs.next()){
//                persons.add(
//                        new Person(
//                                rs.getInt("id"),
//                                rs.getString("name"),
//                                rs.getInt("age")
//                        )
//                );
//            }
//            return persons;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    Person person(int id) {
//        try (
//                var connection = dataSource.getConnection();
//                var statement = connection.prepareStatement("SELECT id, name, age FROM city where id = ?")
//        ) {
//            statement.setInt(1, id);
//            var rs = statement.executeQuery();
//            if (rs.next()) {
//                return new Person(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getInt("age")
//                );
//            }
//            return null;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    void save(Node node) throws RuntimeException {
        int fileId;
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement( "INSERT INTO file(path) VALUES(?);", RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, node.path);
            statement.executeUpdate();
            var rs = statement.getGeneratedKeys();
            rs.next();
            fileId = rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement( "INSERT INTO node(name,value,file_id) VALUES(?,?,?);");
        ) {
            statement.setString(1, node.name);
            statement.setString(2, node.value);
            statement.setInt(3, fileId);
            statement.executeUpdate();
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
                    var statement = connection.prepareStatement("SELECT DISTINCT file.path as path " +
                            "FROM node JOIN file " +
                            "ON node.file_id= file.id " +
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

//    public void update(Person person) {
//        try (
//                var connection = dataSource.getConnection();
//                var statement = connection.prepareStatement("UPDATE city SET name=?,age=? WHERE id = ?");
//        ) {
//            statement.setString(1, person.name);
//            statement.setInt(2, person.age);
//            statement.setInt(3, person.id);
//            var res = statement.executeUpdate();
//            if (res == 0) throw new RuntimeException("No person with id " + person.id );
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void delete(int id) {
//        try (
//                var connection = dataSource.getConnection();
//                var statement = connection.prepareStatement("Delete FROM  city  WHERE id = ?");
//        ) {
//            statement.setInt(1, id);
//            var res = statement.executeUpdate();
//            if (res == 0) throw new RuntimeException();
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}


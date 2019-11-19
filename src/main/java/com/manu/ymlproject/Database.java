package com.manu.ymlproject;

import com.mysql.cj.MysqlType;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Component
public class Database {
    private final DataSource dataSource;
    private Pattern datePattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void save(Yaml yaml) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try(var statement = connection.prepareStatement( "INSERT INTO tree(filePath) VALUES(?);", RETURN_GENERATED_KEYS)) {
                statement.setString(1, yaml.filePath);
                statement.executeUpdate();
                var rs = statement.getGeneratedKeys();
                rs.next();
                yaml.id = rs.getInt(1);
            }
            try (
                    var statement1 = connection.prepareStatement( "INSERT INTO node(attribute_id, tree_id) VALUES(?,?);");
                    var statement_string = connection.prepareStatement( "INSERT INTO string_value(id, value) VALUES(?,?);");
                    var statement_number = connection.prepareStatement( "INSERT INTO number_value(id, value) VALUES(?,?);");
                    var statement_date = connection.prepareStatement( "INSERT INTO date_value(id, value) VALUES(?,?);")
                    ) {
                for (var node : yaml.dbNodes) {
                    if (! node.value.equals("null")){
                        var attribute = getAttribute(node.name, node.value,  connection);
                        statement1.setInt(1, attribute.id);
                        statement1.setInt(2, yaml.id);
                        statement1.addBatch();

                        if (attribute.type == 0){ //Number
                            statement_number.setInt(1, attribute.id);
                            statement_number.setBigDecimal(2, new BigDecimal(node.value));
                            statement_number.addBatch();
                        } else if (attribute.type == 1){ //String
                            statement_string.setInt(1, attribute.id);
                            statement_string.setString(2, node.value);
                            statement_string.addBatch();
                        } else if (attribute.type == 2) { //Date
                            statement_date.setInt(1, attribute.id);
                            statement_date.setDate(2,
                                                        java.sql.Date.valueOf(
                                                            LocalDate.parse(
                                                                node.value, dateFormat)));
//                            LocalDate ld = LocalDate.parse(node.value, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
//                            statement_date.setObject(2, ld, MysqlType.DATE);
                            statement_date.addBatch();
                        }
                    }
                }
                statement1.executeBatch();
                statement_number.executeBatch();
                statement_string.executeBatch();
                statement_date.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Attribute getAttribute(String nodeName, String nodeValue, Connection connection){
        var existingAttribute = getExistingAttribute(nodeName, connection);
        if (existingAttribute != null) return existingAttribute;
        return createNewAttribute(nodeName, nodeValue, connection);
    }

    private Attribute getExistingAttribute(String nodeName, Connection connection) {
        try (var statement = connection.prepareStatement("SELECT id, type_id FROM attribute WHERE attribute.name = ?");) {
            statement.setString(1, nodeName);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) return null;
            return new Attribute(rs.getInt("id"), nodeName, rs.getInt("type_id"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Attribute createNewAttribute(String nodeName, String nodeValue, Connection connection){
        int typeId = guessType(nodeValue);
        try (var statement = connection.prepareStatement("INSERT INTO attribute(name, type_id) VALUES(?,?);", RETURN_GENERATED_KEYS);) {
            statement.setString(1, nodeName);
            statement.setInt(2, typeId);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            return new Attribute(rs.getInt(1), nodeName, typeId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer guessType(String nodeValue){
        try{
            Double.parseDouble(nodeValue);
            return 0;//Number
        } catch (NumberFormatException | NullPointerException nfe) {}
        if (this.datePattern.matcher(nodeValue).matches()) {
            return 2;//Date
        }
        return 1;//String
    }

    ArrayList<Attribute> attributes(){
        try (
                var connection = dataSource.getConnection();
                var statement = connection.prepareStatement("SELECT DISTINCT name,type_id " +
                        "FROM node JOIN attribute " +
                        "ON node.attribute_id= attribute.id " +
                        "ORDER BY name");
        ) {
            ResultSet rs = statement.executeQuery();
            var arrStr = new ArrayList<Attribute>();
            while (rs.next()) {
                arrStr.add(new Attribute(null, rs.getString("name"), rs.getInt("type_id"))); //TODO: null bof...
            }
            return arrStr;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<String> paths(QueryForm[] queryForms){
        try (var connection = dataSource.getConnection()) {
            ResultSet rs = getPathResultSet(queryForms[0], connection);
            ArrayList<String> arrStr = new ArrayList<>();
            while (rs.next()) {
                arrStr.add(rs.getString("path"));
            }
            if (queryForms.length > 1) {
                for (int i = 1; i < queryForms.length; i++) {
                    rs = getPathResultSet(queryForms[i], connection);
                    if (queryForms[i].logic.equals("AND")) {
                        ArrayList<String> newArrStr = new ArrayList<>();
                        while (rs.next()) {
                            String path = rs.getString("path");
                            if (arrStr.contains(path)) {
                                newArrStr.add(path);
                            }
                        }
                        arrStr = newArrStr;
                    } else if (queryForms[i].logic.equals("OR")) {
                        while (rs.next()) {
                            String path = rs.getString("path");
                            if (!arrStr.contains(path)) {
                                arrStr.add(path);
                            }
                        }
                    }
                }
            }
            return arrStr;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    ResultSet getPathResultSet (QueryForm queryForm, Connection connection) throws SQLException {
        String valueCompleteForm = getValueCompleteForm(queryForm.value, queryForm.operator);
        String sql = sql(sqlOperator(queryForm.operator), getValueTable(queryForm.name, connection));
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, queryForm.name);
        var type = getValueTable(queryForm.name, connection);
        switch (type){
            case "number_value":
                statement.setDouble(2, Integer.parseInt(queryForm.value));
                break;
            case "string_value":
                statement.setString(2, valueCompleteForm);
                break;
            case "date_value":
                var ld = LocalDate.parse(queryForm.value, this.dateFormat);
                statement.setObject(2, ld, MysqlType.DATE);
                break;
            default:
                throw new RuntimeException("Unknown type " + type);
        }
        return statement.executeQuery();
    }

    String getValueCompleteForm(String value, String operator){
        switch (operator) {
            case "=":
            case "equals":
            case "<":
            case ">":
            case "on":
            case "before":
            case "after":
                return value;
            case "start with":
                return value + "%";
            case "end with":
                return "%" + value;
            case "contains":
                return "%" + value + "%";
            default:
                throw new RuntimeException("Unknown operator " + operator);
        }
    }

    String sqlOperator(String operator) {
        switch (operator) {
            case "=":
            case "equals":
            case "on":
                return "= ?";
            case "<":
            case "before":
                return "< ?";
            case ">":
            case "after":
                return "> ?";
            case "start with":
            case "end with":
            case "contains":
                return "LIKE ?";
            default:
                throw new RuntimeException("Unknown operator " + operator);
        }
    }
    String sql(String sqlOperator, String valueTable){
        return "SELECT DISTINCT tree.filePath as path " +
                "FROM node " +
                "JOIN tree " +
                "ON node.tree_id = tree.id " +
                "JOIN attribute " +
                "ON node.attribute_id = attribute.id " +
                "JOIN " + valueTable +
                " ON attribute.id = " + valueTable + ".id " +
                "WHERE attribute.name = ? AND " + valueTable + ".value " + sqlOperator;
    }

    String getValueTable (String name, Connection connection) {
        try{
            var statement = connection.prepareStatement("SELECT DISTINCT type_id " +
                    "FROM node JOIN attribute " +
                    "ON node.attribute_id= attribute.id " +
                    "WHERE attribute.name = ?");
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            rs.next();
            var typeId = rs.getInt(1);
            switch (typeId){
                case 0:
                    return "number_value";
                case 1:
                    return "string_value";
                case 2:
                    return "date_value";
                default:
                    throw new RuntimeException("Unknown type " + typeId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}


package com.keivsc.SQLiteJava;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

/**
 * Columns in the current table
 */
class Columns{
    private final List<Map<String, Object>> fieldValues = new ArrayList<>();
    public final List<Map<String, Object>> columnValues = new ArrayList<>();
    private static final Map<String, Class<?>> types = new HashMap<>();
    //        NULL- It is a NULL value.
//        INTEGER- It is an integer, stored in 1, 2, 3, 4, 6, or 8 bytes depending on the value.
//        REAL- It is a floating point value, stored as an 8-byte floating number.
//        TEXT- It is a string, stored using the database encoding (UTF).
//        BLOB- It is a group of data, stored exactly as it was entered.
    static {
        types.put("INTEGER", Integer.class);
        types.put("REAL", double.class);
        types.put("TEXT", String.class);
        types.put("BLOB", byte[].class);
    }

    private Class<?> parseTypes(String type){
        return types.get(type);
    }


    public Columns(List<Map<String, Object>> items){
        for (Map<String, Object> item : items) {
            Map<String, Object> fieldValue = new HashMap<>();
            Map<String, Object> columnValue = new HashMap<>();
            String name = (String) item.get("name");
            Object type = parseTypes((String) item.get("type"));
            fieldValue.put(name, type);
            fieldValue.put("primary_key", item.get("primary_key"));
            columnValue.put("columnName", name);
            columnValue.put("columnType", (String) item.get("type"));
            columnValue.put("primary_key", item.get("primary_key"));
            fieldValues.add(fieldValue);
            columnValues.add(columnValue);
        }
    };


    public Object getType(String key){
        for (Map<String, Object> fieldValue : fieldValues) {
            if (fieldValue.containsKey(key)) {
                return fieldValue.get(key);
            }
        }
        return null;
    }

    public int size(){
        return fieldValues.size();
    }

}

/**
 * Initialize Table Class
 */
public class Table {
    public String Name;
    private Columns Column;
    private Connection conn;

    /**
     * Initialize the table class
     * @param tableName String | Name of the table
     * @param items List&lt;Map&lt;String, Object&gt;&gt; | List of items
     * @param conn {@code Connection} | SQLite Connection
     */
    public Table(String tableName, List<Map<String, Object>>  items, Connection conn){

        this.Name = tableName;
        this.conn = conn;
        this.Column = new Columns(items);
    };

    /**
     * Get the current table columns
     * @return <code>List&lt;Map&lt;String,Object&gt;&gt;</code> | List of keys and types of the columns
     */
    public List<Map<String, Object>> getColumns(){
        return this.Column.columnValues;
    }

    /**
     * Runs a SQL Command directly with no formatting
     * @param command String | SQL Command following SQLite Formatting
     * @throws Errors.CommandException Command Exception
     */
    public void runCommand(String command) throws Errors.CommandException {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(command);
        } catch (SQLException e) {
            throw new Errors.CommandException(e.getMessage());
        }
    }


    /**
     * Runs a SQL Query
     * @param query String | SQL Query following the SQLite Formatting
     * @return <code>java.sql.ResultSet</code>
     * @throws Errors.QueryException Query Exception
     */
    public ResultSet runQuery(String query) throws Errors.QueryException {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        }catch (SQLException e) {
            throw new Errors.QueryException(e.getMessage());
        }
    }


    /**
     * Clears the entire table, This is dangerous and should almost never be used
     * @throws Errors.TableException Table Exception
     */
    public void clearTable() throws Errors.TableException {
        try {
            this.runCommand("DELETE * FROM " + Name);
        }catch (SQLException e) {
            throw new Errors.TableException("Error Clearing Table: "+e.getMessage());
        }
    }

    /**
     * Add item to the current table
     * @param items Value
     * @throws Errors.TableException Table Exception
     */
    public void addItem(Value items) throws Errors.TableException {
        for (var entry : items.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (items.get(key).getClass() != this.Column.getType(key)){
                throw new Errors.TableException("Item '" + key + "' is in the wrong Type");
            }
        }
        StringBuilder query = new StringBuilder("INSERT INTO " + Name + " VALUES(");
        for (int i = 0; i < this.Column.size(); i++){
            Object value = items.get(items.keys.get(i));
            if (value.getClass() == String.class || value.getClass() == Byte.class){
                query.append("'" + value + "'");
            }else{
                query.append(value);
            }
            if (i != this.Column.size() - 1){
                query.append(",");
            }
        }
        query.append(")");
        try {
            this.runCommand(query.toString());
        }catch (SQLException e){
            throw new Errors.TableException(e.getMessage());
        }
    }

    /**
     * Edit an item in the current table, if identifer found more than one item, the operation will not go through (SQLException is thrown)
     * @param Identifier String | SQLite WHERE <code>{identifier}</code>
     * @param items Value
     * @throws Errors.TableException Table Exception
     */
    public void editItem(String Identifier, Value items) throws Errors.TableException {
        for (var entry : items.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (items.get(key) != this.Column.getType(key)){
                throw new Errors.TableException("Item '" + key + "' is in the wrong Type");
            }
        }
        List<Value> oldItem = this.getItems(Identifier);
        if (oldItem.size() > 1){
            throw new Errors.TableException("Multiple items with the same identifier '" + Identifier + "'");
        }else {
            StringBuilder query = new StringBuilder("INSERT INTO " + Name + "VALUES(");
            for (int i = 0; i < this.Column.size(); i++) {
                query.append("?");
                if (i != this.Column.size() - 1) {
                    query.append(",");
                }
            }
            query.append("WHERE " + Identifier + ")");
            try {
                this.runCommand(query.toString());
            }catch (SQLException e){
                throw new Errors.TableException(e.getMessage());
            }
        }
    }

    /**
     * Get a list of items following the identifer, Identifier can be any length
     * @param identifier String | SQLite WHERE <code>{identifier}</code>
     * @return <code>List&lt;Value&gt;</code> Returns a list of Values
     * @throws Errors.TableException Table Exception
     */
    public List<Value> getItems(String identifier) throws Errors.TableException {
        try {
            StringBuilder query = new StringBuilder("SELECT * ");
            query.append(" FROM ").append(Name).append(" WHERE " + identifier);
            ResultSet rs = runQuery(query.toString());
            List<Map<String, Object>> itemValues = new ArrayList<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    item.put(columnName, columnValue);
                }
                itemValues.add(item);
            }
            List<Value> fieldValues = new ArrayList<>();
            for (Map<String, Object> item : itemValues) {
                Value items = new Value();
                items.ValuesInit(itemValues);
                fieldValues.add(items);
            }
            return fieldValues;
        }catch (SQLException e){
            throw new Errors.TableException(e.getMessage());
        }
    };

    /**
     * Get all items from the current table
     * @return <code>List&lt;Value&gt;</code> Returns a list of Values
     * @throws Errors.TableException Table Exception
     */
    public List<Value> getAllItems() throws Errors.TableException {
        try {
            ResultSet rs = runQuery("SELECT * " + " FROM " + Name);
            List<Map<String, Object>> itemValues = new ArrayList<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    item.put(columnName, columnValue);
                }
                itemValues.add(item);
            }
            List<Value> fieldValues = new ArrayList<>();
            for (Map<String, Object> item : itemValues) {
                Value items = new Value();
                items.ValuesInit(itemValues);
                fieldValues.add(items);
            }
            return fieldValues;
        }catch (SQLException e){
            throw new Errors.TableException(e.getMessage());
        }
    }

    /**
     * Get a list of values from items following the identifer, Identifier can be any length
     * @param identifier String | SQLite WHERE <code>{identifier}</code>
     * @param values String[] | The values you want to get
     * @return <code>List&lt;Value&gt;</code> Returns a list of Values
     * @throws Errors.TableException Table Exception
     */
    public List<Value> getValues(String identifier, String[] values) throws Errors.TableException {
        try {
            StringBuilder query = new StringBuilder("SELECT ");
            for (int i = 0; i < values.length; i++) {
                query.append(values[i]);
                if (i != values.length - 1) {
                    query.append(", ");
                }
            }
            query.append(" FROM ").append(Name).append(" WHERE ").append(identifier);
            ResultSet rs = runQuery(query.toString());
            List<Map<String, Object>> itemValues = new ArrayList<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    item.put(columnName, columnValue);
                }
                itemValues.add(item);
            }
            List<Value> fieldValues = new ArrayList<>();
            for (Map<String, Object> item : itemValues) {
                Value items = new Value();
                items.ValuesInit(itemValues);
                fieldValues.add(items);
            }
            return fieldValues;
        }catch (SQLException e){
            throw new Errors.TableException(e.getMessage());
        }
    }

    /**
     * Delete item from the table, if more than one item is found, all items will be deleted
     * @param identfier String | SQLite WHERE <code>{identifier}</code>
     * @throws Errors.TableException Table Exception
     */
    public void deleteItem(String identfier) throws Errors.TableException {
        try {
            this.runCommand("DELETE FROM " + Name + " WHERE " + identfier);
        }catch (SQLException e){
            throw new Errors.TableException(e.getMessage());
        }
    }

    /**
     * Convert the current table to a JSONObject
     * @return <code>JSONObject</code>
     * @throws Errors.TableException Table Exception
     */
    public JSONObj toJSON() throws Errors.TableException {
        try {
            List<Value> allItems = this.getAllItems();
            Map<String, Object> table = new HashMap<>();
            List<Object> itemValues = new ArrayList<>();
            for (Value item : allItems) {
                itemValues.add(item.data);
            }
            table.put(this.Name, itemValues);
            return new JSONObj(table);
        }catch(SQLException e){
            throw new Errors.TableException("Error Converting to JSON: "+e.getMessage() );
        }
    }

}


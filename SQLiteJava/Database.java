package com.keivsc.SQLiteJava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;z

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    static class databaseMethods{
        public static final int CreateTable = 0;
        public static final int ClearTable = 1;
        public static final int DeleteTable = 2;
        public static final int InsertItems = 3;
        public static final int UpdateItems = 4;
        public static final int DeleteItems = 5;
        public static final int SelectItems = 6;
        public static final int SelectAllItems = 7;

    }

    private Connection conn;
    private boolean debug;
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    /**Get every column name, type and other attributes from a table
     *
     * @param tableName String | The table's name
     * @return <code>List&lt;Map&lt;String,Object&gt;&gt;</code> List of each column as a map
     * @throws Errors.DatabaseException
     *
     *
     */

    private List<Map<String, Object>> getTableInfo(String tableName) throws Errors.DatabaseException {
        try{
            Statement stmt = this.conn.createStatement();

            // Execute the PRAGMA table_info query
            String sql = "PRAGMA table_info(" + tableName + ")";
            ResultSet rs = stmt.executeQuery(sql);

            // Get the table information
            List<Map<String, Object>> tableInfo = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> columnInfo = new HashMap<>();
                columnInfo.put("name", rs.getString("name"));
                columnInfo.put("type", rs.getString("type"));
                columnInfo.put("notnull", rs.getInt("notnull") == 1);
                columnInfo.put("default_value", rs.getString("dflt_value"));
                columnInfo.put("primary_key", rs.getInt("pk") == 1);
                tableInfo.add(columnInfo);
            }

            return tableInfo;
        }catch (SQLException e) {
            throw new Errors.DatabaseException("Error executing SQL statement: " + e.getMessage());
        }

    }


    /** Initialize the Database / Connects to the database file
     *
     * @param filename String | Name of the database file you are accessing ie "Example.db"
     * @throws Errors.DatabaseException Database Exception
     */
    public Database(String filename) throws Errors.DatabaseException {
        String dbUrl = "jdbc:sqlite:" + filename;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("SQLite JDBC driver not found. Please add it to your project.", e);
            throw new Errors.DatabaseException("SQLite JDBC driver not found. Please add it to your project.");
        }

        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            if (conn != null) {
                this.conn = conn;
            }
        } catch (SQLException e) {
            logger.error("Could not connect to database.", e);
            throw new Errors.DatabaseException("Could not connect to database: " + e.getMessage());
        }
    }

    /**
     * Ends the database connection / use this when updating the database file.
     * @throws Errors.DatabaseException Database Exception
     */
    public void close() throws Errors.DatabaseException {
        try{
            this.conn.close();
        }catch(SQLException e) {
            throw new Errors.DatabaseException("Could not close database connection: " + e.getMessage());
        }
    }

    /**
     * Create a table in the current database
     * @param tableName String | The name of the table
     * @param items String[] | The name and types of the columns
     * @return Table | Returns a Table Class
     * @throws Errors.TableException Table Exception
     */
    public Table createTable(String tableName, String[] items) throws Errors.TableException {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        for (int i = 0; i < items.length; i++) {
            query.append(items[i]);
            if (i < items.length - 1) {
                query.append(", ");
            }
        }
        query.append(");");
        try {
            Statement stmt = this.conn.createStatement();
            stmt.execute(query.toString());
            return new Table(tableName, getTableInfo(tableName), this.conn);
        }catch(SQLException e) {
            throw new Errors.TableException("Error executing SQL statement: " + e.getMessage());
        }
    }

    /**
     * Connect to an existing Table
     * @param tableName String | The name of the table
     * @return {@code Table} / {@code null} | Table class, Returns null if table is not found
     */
    public Table connectTable(String tableName) {
        try{
            List<Map<String, Object>> tableInfo = getTableInfo(tableName);
            if (tableInfo.isEmpty()) {
                return null;
            }
            return new Table(tableName, tableInfo, this.conn);
        } catch (Errors.DatabaseException e) {
            return null;
        }
    }

    /**
     * Converts the current database and all its tables to json format
     * @return <code>JSONObject</code>
     * @throws SQLException
     */
    public JSONObj toJSON() throws SQLException {
        JSONObj json = new JSONObj();
        Statement stmt = this.conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        while (rs.next()) {
            List<Object> itemValues = new ArrayList<>();
            Table currentTable = new Table(rs.getString("name"), getTableInfo(rs.getString("name")), this.conn);
            List<Value> allItems = currentTable.getAllItems();
            for (Value item : allItems) {
                itemValues.add(item.data);
            }
            json.put(rs.getString("name"), itemValues);
        }
        return json;
    }
}

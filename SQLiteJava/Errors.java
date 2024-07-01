package com.keivsc.SQLiteJava;

import java.sql.SQLException;

/**
 * Class of errors
 */
public class Errors {

    /**
     * Private constructor to prevent instantiation of Errors class.
     */
    private Errors() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }


    /**
     * SuperException Database | Returns message
     */
    public static class DatabaseException extends SQLException {
        /**
         * SuperException Database | Returns message
         * @param message String | Message
         */
        public DatabaseException(String message) {
            super(message);
        }
    }

    /**
     * Exception for runQuery | Returns message
     */
    public static class QueryException extends DatabaseException {
        /**
         * Exception for runQuery | Returns message
         * @param message String | Message
         */
        public QueryException(String message) {
            super(message);
        }
    }

    /**
     * Exception for any Table related functions | Returns message
     */
    public static class TableException extends DatabaseException {
        /**
         * Exception for any Table related functions | Returns message
         * @param message String | Message
         */
        public TableException(String message) {
            super(message);
        }
    }

    /**
     * Exception for runCommand | Returns message
     */
    public static class CommandException extends DatabaseException {
        /**
         * Exception for runCommand | Returns message
         * @param message String | Message
         */
        public CommandException(String message) {
            super(message);
        }
    }
}

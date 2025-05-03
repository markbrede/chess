package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }

                Properties props = new Properties();
                props.load(propStream);

                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));

                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }

            DatabaseManager.createDatabase();
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);

            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            createTables();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    static void createTables() throws DataAccessException {
        String[] createStatements = {
                """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(50) PRIMARY KEY,
            password TEXT NOT NULL,
            email TEXT NOT NULL
        )
        """,
                """
        CREATE TABLE IF NOT EXISTS auth (
            authToken VARCHAR(50) PRIMARY KEY,
            username TEXT
        )
        """,
                """
        CREATE TABLE IF NOT EXISTS game (
            gameID INTEGER PRIMARY KEY,
            whiteUsername TEXT,
            blackUsername TEXT,
            gameName TEXT,
            game TEXT,
            gameOver BOOLEAN NOT NULL DEFAULT FALSE
        )
        """
        };

        clearTables();

        try {
            var conn = getConnection();

            try (var statement = conn.createStatement()) {
                for (String sql : createStatements) {
                    statement.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    static void clearTables() throws DataAccessException {
        String[] dropStatements = {
                "DROP TABLE IF EXISTS user",
                "DROP TABLE IF EXISTS auth",
                "DROP TABLE IF EXISTS game"
        };

        try {
            var conn = getConnection();

            try (var statement = conn.createStatement()) {
                for (String sql : dropStatements) {
                    statement.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}

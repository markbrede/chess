package dataaccess;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    //Will hold database configuration values loaded from a file
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    public static void main(String[] args) throws DataAccessException {
        DatabaseManager.createTables();
    }

    /**
     * Static block runs once when the class is first loaded.
     * It loads database configuration values from the db.properties file.
     */
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

                //get the host and port to build the database connection URL
                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));

                //make the JDBC connection URL for MySQL
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it doesn't already exist.
     * This is done by connecting to the MySQL server and executing a CREATE DATABASE statement.
     */
    static void createDatabase() throws DataAccessException {
        try {

            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;

            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);

            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate(); // Run the SQL command
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Opens a connection to the database defined in the db.properties file.
     * It sets the catalog (i.e., selects the database) after connecting.
     * Always use try-with-resources to ensure the connection gets closed properly.
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //connect to server
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
        CREATE TABLE user (
            username VARCHAR(50) PRIMARY KEY,
            password TEXT,
            email TEXT
        )
        """,
                """
        CREATE TABLE auth (
            authToken VARCHAR(50) PRIMARY KEY,
            username TEXT
        )
        """,
                """
        CREATE TABLE game (
            gameID INTEGER PRIMARY KEY,
            whiteUsername TEXT,
            blackUsername TEXT,
            gameName TEXT,
            game TEXT
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
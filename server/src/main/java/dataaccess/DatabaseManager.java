package dataaccess;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    // These constants will hold database configuration values loaded from a file
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    public static void main(String[] args) throws DataAccessException {
        DatabaseManager.createTables();
    }

    /*
     * Static block runs once when the class is first loaded.
     * It loads database configuration values from the db.properties file.
     */
    static {
        try {
            // Try to load the db.properties file from the classpath
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    // Throw an error if the file isn't found
                    throw new Exception("Unable to load db.properties");
                }

                // Load properties into a Properties object
                Properties props = new Properties();
                props.load(propStream);

                // Extract values from the properties file
                DATABASE_NAME = props.getProperty("db.name");      // e.g., "mydatabase"
                USER = props.getProperty("db.user");              // e.g., "root"
                PASSWORD = props.getProperty("db.password");      // e.g., "password"

                // Get the host and port to build the database connection URL
                var host = props.getProperty("db.host");          // e.g., "localhost"
                var port = Integer.parseInt(props.getProperty("db.port")); // e.g., 3306

                // Construct the JDBC connection URL for MySQL
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            // If anything goes wrong, stop the program with a clear message
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it doesn't already exist.
     * This is done by connecting to the MySQL server and executing a CREATE DATABASE statement.
     */
    static void createDatabase() throws DataAccessException {
        try {
            // SQL command to create the database if it doesn't already exist
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;

            // Open a connection to the MySQL server using the URL, user, and password
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);

            // Use try-with-resources to auto-close the statement when done
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate(); // Run the SQL command
            }
        } catch (SQLException e) {
            // If any SQL error occurs, wrap and rethrow as a custom exception
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

            // Set the default database (catalog) to the one we loaded from the properties
            conn.setCatalog(DATABASE_NAME);

            // Return the open connection to the caller
            return conn;
        } catch (SQLException e) {
            // Wrap and rethrow any SQL errors
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
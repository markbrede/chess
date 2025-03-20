package dataaccess;

/**
 * Indicates there was an error connecting to the database
 * All methods in my MemoryUserDAO class that can encounter data access issues need to throw this exception
 */
public class DataAccessException extends Exception{
    public DataAccessException(String message) {
        super(message);
    }
}

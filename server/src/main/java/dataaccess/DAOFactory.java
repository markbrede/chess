package dataaccess;

public class DAOFactory {
    private static boolean useDatabase = false;

    public static void setUseDatabase(boolean use) {
        useDatabase = use;
    }

    public static UserDAO createUserDAO() throws DataAccessException {
        return useDatabase ?
                initializeDatabaseDAO(new DBUserDAO()) :
                new MemoryUserDAO();
    }

    public static AuthDAO createAuthDAO() throws DataAccessException {
        return useDatabase ?
                initializeDatabaseDAO(new DBAuthDAO()) :
                new MemoryAuthDAO();
    }

    public static GameDAO createGameDAO() throws DataAccessException {
        return useDatabase ?
                initializeDatabaseDAO(new DBGameDAO()) :
                new MemoryGameDAO();
    }

    private static <T> T initializeDatabaseDAO(T dao)
            throws DataAccessException {
        DatabaseManager.createTables();
        return dao;
    }
}

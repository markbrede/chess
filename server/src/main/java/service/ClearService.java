package service;

import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;

public class ClearService {
    private final MemoryUserDAO userDAO;

    public ClearService(MemoryUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void clearAll() throws DataAccessException {
        try {
            userDAO.clear();
            // Clear other DAOs if you have them
        } catch (Exception e) {
            throw new DataAccessException("Error: Failed to clear data");
        }
    }
}

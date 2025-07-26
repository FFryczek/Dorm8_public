package dev.ffryczek.Utilities;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.ExpenseDAO;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.DAOs.PendingRequestDAO;

public final class DBCleaner {
    private DBCleaner() {
        throw new UnsupportedOperationException("\nObject of this class cannot be instantiated.\n");
    }

    private static DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();

    public static void ereaseAll() {
        dataSourceConnectionHelper.connectCredentials();
        PendingRequestDAO pendingRequestDAO = new PendingRequestDAO(dataSourceConnectionHelper);
        ExpenseDAO expenseDAO = new ExpenseDAO(dataSourceConnectionHelper);
        GroupUserDAO groupUserDAO = new GroupUserDAO(dataSourceConnectionHelper);
        GroupDAO groupDAO = new GroupDAO(dataSourceConnectionHelper);
        GlobalUserDAO globalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);

        pendingRequestDAO.cleanUp();
        expenseDAO.cleanUp();
        groupUserDAO.cleanUp();
        groupDAO.cleanUp();
        globalUserDAO.cleanUp();
        

    }

    public void ereaseAll(String url, String user, String password) {
        dataSourceConnectionHelper.connectCredentials(url, user, password);
        PendingRequestDAO pendingRequestDAO = new PendingRequestDAO(dataSourceConnectionHelper);
        ExpenseDAO expenseDAO = new ExpenseDAO(dataSourceConnectionHelper);
        GroupUserDAO groupUserDAO = new GroupUserDAO(dataSourceConnectionHelper);
        GroupDAO groupDAO = new GroupDAO(dataSourceConnectionHelper);
        GlobalUserDAO globalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);

        pendingRequestDAO.cleanUp();
        expenseDAO.cleanUp();
        groupUserDAO.cleanUp();
        groupDAO.cleanUp();
        globalUserDAO.cleanUp();
        
    }
    
}

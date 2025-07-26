package dev.ffryczek.DAOsTested;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Utilities.DBCleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

    

public class GlobalUserDAOTest {

    private GlobalUserDAO testGlobalUserDAO;
    private GlobalUser testGlobalUser;
    private String testLogin = "testlogin_123";
    private String testPassword = "testpassword_123";
    private String userName = "testGlobalUser";
    private UUID id;

    @BeforeEach
    public void setUp() {

        //Creating global User
        testGlobalUser = new GlobalUser(userName);
        id = testGlobalUser.getUserID();

        //Creating DataSourceConnectionHelper object
        DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();

        // Connection details 
        String url = "jdbc:mysql://localhost:3306/Drom8DB";
        String password = "fici=me5ma";
        String user = "root";

        //Establishing connection
        dataSourceConnectionHelper.connectCredentials(url, user, password);

        //Creating DAO object
        testGlobalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);


    }
    
    @AfterEach
    public void cleanUp() {
        DBCleaner.ereaseAll();
    }


    @Test
    public void insert_and_withdraw_new_global_user() {

        //Inserting to database
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);

        //Withdrawind from database
        GlobalUser withdrawnGlobalUser = testGlobalUserDAO.withdrawGlobalUser(testLogin, testPassword);

        assertNotNull(withdrawnGlobalUser);
        assertEquals(id, withdrawnGlobalUser.getUserID());
        System.out.println(withdrawnGlobalUser.getUserID());
        assertEquals(userName, withdrawnGlobalUser.getName());
        System.out.println(withdrawnGlobalUser.getName());

    }
    
    @Test
    public void withdraw_invalid_user() {
        //Inserting to database
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);

        //Create invalid login (not existing in DB)
        String wrongTestLogin = "wrongTestLogin";

        assertThrows(IllegalArgumentException.class, () -> {
            //Withdrawind from database
            GlobalUser withdrawnGlobalUser = testGlobalUserDAO.withdrawGlobalUser(wrongTestLogin, testPassword);
        });

    }
    
    @Test 
    public void withdraw_userID_on_login() {
        //Inserting to db
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        //Get UUID of freshly created user
        UUID storedUUID = testGlobalUser.getUserID();

        //Store UUID of user withdrawn by login
        UUID fetchedUUID = testGlobalUserDAO.withdrawGlobalUser_onLogin(testLogin);

        assertEquals(fetchedUUID, storedUUID);
    }

}

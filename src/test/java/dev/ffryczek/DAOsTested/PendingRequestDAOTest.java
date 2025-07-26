package dev.ffryczek.DAOsTested;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.PendingRequestDAO;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.PendingRequest;

public class PendingRequestDAOTest {

    //DAOs
    private PendingRequestDAO testRequestDAO;
    private GlobalUserDAO testGlobalUserDAO;
    private GroupDAO testGroupDAO;

    //Credentials
    private String testLogin = "testlogin_123";
    private String userName = "testGlobalUser";

    private String friendLogin = "friendlogin_123";
    private String friendName = "testGlobalFriend";

    private String testPassword = "testpassword_123";

    //Objects
    private GlobalUser testGlobalUser = new GlobalUser(userName);
    private GlobalUser testGlobalFriend = new GlobalUser(friendName);

    private Group testGroup = new Group("testGroup");

    //Storing IDs for convenience
    private UUID friendUserID = testGlobalFriend.getUserID();
    private UUID globalUserID = testGlobalUser.getUserID();

    private UUID groupID = testGroup.getGroupID();

    //Create requests
    private PendingRequest pendingRequest = new PendingRequest(groupID, friendUserID, globalUserID);


    @BeforeEach
    public void setUp() {
        //Create DataSourceConnectionHelper object
        DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();

        dataSourceConnectionHelper.connectCredentials();

        //Create DAO objects
        testGroupDAO = new GroupDAO(dataSourceConnectionHelper);
        testRequestDAO = new PendingRequestDAO(dataSourceConnectionHelper);
        testGlobalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);

        //Insert both new global user and group to DB
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        testGlobalUserDAO.insertGlobalUser(testGlobalFriend, friendLogin, testPassword);
        testGroupDAO.insertGroup(testGroup);
    }

    @AfterEach
    public void cleanUp() {
        //Delete all
        testRequestDAO.deleteRequest(pendingRequest.getRequestID());
        testGroupDAO.deleteGroup(groupID);
        testGlobalUserDAO.deleteGlobalUser(friendLogin, testPassword);
        testGlobalUserDAO.deleteGlobalUser(testLogin, testPassword);
    }

    @Test 
    public void insert_and_withdraw_pending_request () {
        //Inserting to db
        testRequestDAO.insertRequest(pendingRequest);

        //Withdraw from db
        PendingRequest fetchedPendingRequest = testRequestDAO.withdrawRequest(pendingRequest.getRequestID());

        //Assert equals (FROM,TO,GROUPID,REQUESTID)
        assertEquals(pendingRequest.getFromID(), fetchedPendingRequest.getFromID());
        assertEquals(pendingRequest.getInvitedGlobalUserID(), fetchedPendingRequest.getInvitedGlobalUserID());
        assertEquals(pendingRequest.getToGroupID(), fetchedPendingRequest.getToGroupID());
        assertEquals(pendingRequest.getRequestID(), fetchedPendingRequest.getRequestID());
        
    }

}

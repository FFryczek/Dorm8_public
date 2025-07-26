package dev.ffryczek.ServicesTested;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Services.GroupJoinerService;
import dev.ffryczek.Services.GroupManagerService;
import dev.ffryczek.Services.RequestService;
import dev.ffryczek.Services.UserSessionService;
import dev.ffryczek.Utilities.DBCleaner;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Entities.PendingRequest;

public class GroupManagerServiceTest {

    private GroupJoinerService testGJS;
    private GroupManagerService testGMS;
    private UserSessionService testUSS1;
    private UserSessionService testUSS2;
    private RequestService testRS;
    private DataSourceConnectionHelper dataSourceConnectionHelper;

     //User credentials
    private String testLogin = "testlogin_123";
    private String userName = "testGlobalUser";

    private String friendLogin = "friendlogin_123";
    private String friendName = "testGlobalFriend";

    private String testPassword = "testpassword_123";

    //Objects
    private GlobalUser testGlobalUser = new GlobalUser(userName);
    private GlobalUser testFriendUser = new GlobalUser(friendName);

    //UUIDs
    private UUID testGlobalUserID = testGlobalUser.getUserID();
    private UUID testFriendUserID = testFriendUser.getUserID();


    @BeforeEach
    public void setUp() {
        //Create DataSource, use default credentials and Services
        dataSourceConnectionHelper = new DataSourceConnectionHelper();
        dataSourceConnectionHelper.connectCredentials();

        this.testUSS1 = new UserSessionService(dataSourceConnectionHelper);
        this.testUSS2 = new UserSessionService(dataSourceConnectionHelper);
        testUSS1.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        testUSS1.insertGlobalUser(testFriendUser, friendLogin, testPassword);

        //Log in (assign globalUser to UserSessionService)
        this.testUSS1.assignGlobalUser(testLogin, testPassword);
        this.testUSS2.assignGlobalUser(friendLogin, testPassword);


        //Access create group menu
        this.testGMS = new GroupManagerService(testUSS1.getMyGlobalUserID(),
                dataSourceConnectionHelper);

    }

    @AfterEach
    public void cleanUp() {
        DBCleaner.ereaseAll();
    }


    @Test
    public void insert_and_withdraw_group_using_service() {
        //Create and insert group
        UUID fetchedGroupID = this.testGMS.groupCreatorTool("testGroup1");
        //Withdraw
        Group fetchedGroup = this.testGMS.withdrawGroup(fetchedGroupID);
        //Assertions
        assertNotEquals(fetchedGroup, null);
        assertEquals(fetchedGroup.getName(), "testGroup1");
    }

    @Test
    public void add_user_to_group() {
        //Create and insert group
        UUID createdGroupID = this.testGMS.groupCreatorTool("testGroup1");

        //Create request
        this.testRS = new RequestService(testUSS1.getMyGlobalUserID(), dataSourceConnectionHelper);

        //Send invite to user (create object and insert to db)
        this.testRS.sendRequest(createdGroupID, testUSS1.getMyGlobalUserID(),
                testUSS1.withdrawGlobalUser_onLogin(friendLogin));
        
        //Simulate accepting invite
        this.testUSS2.initialLoad();
        TreeMap<UUID, PendingRequest> userRequests = this.testUSS2.getRequests();
        
        ArrayList<UUID> userRequestsArr = new ArrayList<>(userRequests.keySet());

        this.testGJS = new GroupJoinerService(this.testUSS2.getMyGlobalUserID(), dataSourceConnectionHelper);

        //Assesing request
        try{
        this.testGJS.assessRequest(true, userRequestsArr.get(0));
        }
        catch (AccessException e) {
            System.out.println("Unable to access this request");
            e.printStackTrace();
        }

        System.out.println("-------------");
        //REFRESH AFTER ACCEPTING REQUEST
        this.testUSS2.initialLoad();

        //Withdraw
        TreeMap<UUID, UUID> fetchedGroupUserIDsByGroupID = this.testUSS2.getMyGroupUserIDsByGroupID();
        System.out.println(fetchedGroupUserIDsByGroupID.toString());
        UUID friendGroupUserID = fetchedGroupUserIDsByGroupID.get(createdGroupID);
        TreeMap<UUID, GroupUser> fetchedMap = this.testGMS.withdrawGroupMembers();

        int numberOfUsers = fetchedMap.size();

        //Print
        System.out.println(testUSS2.getGroups().toString());
        System.out.println("-------------");
        System.out.println(testUSS2.getGroupUsers().toString());
        System.out.println("-------------");
        System.out.println(fetchedMap.toString());
        //Assertions
        assertNotEquals(fetchedMap, null);
        assertEquals(numberOfUsers, 2);
        assertTrue(fetchedMap.containsKey(friendGroupUserID));

    }


}

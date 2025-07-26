package dev.ffryczek.ServicesTested;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Services.GroupManagerService;
import dev.ffryczek.Services.UserSessionService;
import dev.ffryczek.Utilities.DBCleaner;

public class UserSesionServiceTest {

    private UserSessionService testUserSessionService;
    private GroupManagerService testGroupManagerService;
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



    @BeforeEach
    public void setUp() {
        //Create DataSource, use default credentials and Services
        dataSourceConnectionHelper = new DataSourceConnectionHelper();
        dataSourceConnectionHelper.connectCredentials();

        this.testUserSessionService = new UserSessionService(dataSourceConnectionHelper);
        testUserSessionService.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        testUserSessionService.insertGlobalUser(testFriendUser, friendLogin, testPassword);

    }
    
    @AfterEach
    public void cleanUp() {
        DBCleaner.ereaseAll();
    }


    @Test
    public void validate_my_global_user_id() {
        //Log in (assign globalUser to UserSessionService)
        this.testUserSessionService.assignGlobalUser(testLogin, testPassword);

        //Assert
        assertEquals(this.testUserSessionService.getMyGlobalUserID(), testGlobalUser.getUserID());
    }
    
    @Test
    public void initial_load_test() {
        //Log in (assign globalUser to UserSessionService)
        this.testUserSessionService.assignGlobalUser(testLogin, testPassword);

        //Access create group menu
        this.testGroupManagerService = new GroupManagerService(testUserSessionService.getMyGlobalUserID(),
                dataSourceConnectionHelper);

        //Create and insert two groups using testGroupManager
        this.testGroupManagerService.groupCreatorTool("testGroup1");
        this.testGroupManagerService.groupCreatorTool("testGroup1");

        //Using initial load to fetch all groups User belongs to
        this.testUserSessionService.initialLoad();

        TreeMap<UUID, Group> fetchedGroups = testUserSessionService.getGroups();
        TreeMap<UUID, GroupUser> fetchedGroupUsers = testUserSessionService.getGroupUsers();

        System.out.println(fetchedGroups.toString());
        System.out.println(fetchedGroupUsers.toString());

        //Assertions
        assertNotEquals(fetchedGroups, null);
        assertNotEquals(fetchedGroupUsers, null);

    }
    
    @Test
    public void withdraw_global_user_on_login_test() {
        //Withdraw different user for purpose of adding to group for example
        UUID fetchedUsersID = this.testUserSessionService.withdrawGlobalUser_onLogin(friendLogin);
        //Assertions
        assertEquals(fetchedUsersID, testFriendUser.getUserID());
    }

}

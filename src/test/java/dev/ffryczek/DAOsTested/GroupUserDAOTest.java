package dev.ffryczek.DAOsTested;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Utilities.DBCleaner;

public class GroupUserDAOTest {

    //DAOs
    private GroupUserDAO testGroupUserDAO;
    private GroupDAO testGroupDAO;
    private GlobalUserDAO testGlobalUserDAO;

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

    private GroupUser testGroupUser = new GroupUser(testGlobalUser.getUserID(), testGroup.getGroupID());
    private GroupUser testGroupFriend = new GroupUser(testGlobalFriend.getUserID(), testGroup.getGroupID());

    //Storing IDs for convenience
    private UUID groupUserID = testGroupUser.getGroupUserID();
    private UUID globalUserID = testGlobalUser.getUserID();

    private UUID groupID = testGroup.getGroupID();

    private UUID globalFriendID = testGlobalFriend.getUserID();
    private UUID groupFriendID = testGroupFriend.getGroupUserID();

    @BeforeEach
    public void setUp() {

        //Creating DataSourceConnectionHelper (default conn params)
        DataSourceConnectionHelper dataSourceConnectionHelper = new DataSourceConnectionHelper();

        //Establishing connection with default params
        dataSourceConnectionHelper.connectCredentials();

        //Creating DAO objects
        testGroupDAO = new GroupDAO(dataSourceConnectionHelper);
        testGroupUserDAO = new GroupUserDAO(dataSourceConnectionHelper);
        testGlobalUserDAO = new GlobalUserDAO(dataSourceConnectionHelper);

        //Insert both new global user and group to DB
        testGlobalUserDAO.insertGlobalUser(testGlobalUser, testLogin, testPassword);
        testGlobalUserDAO.insertGlobalUser(testGlobalFriend, friendLogin, testPassword);
        testGroupDAO.insertGroup(testGroup);

    }

    @AfterEach
    public void cleanUp() {
        DBCleaner.ereaseAll();
    }

    @Test
    public void insert_and_withdraw_group_user() {
        //Insert groupUser to db
        testGroupUserDAO.insertGroupUser(testGroupUser, globalUserID, groupID);

        //Withdraw user from db
        GroupUser fetchedGroupUser = testGroupUserDAO.withdrawGroupUser(globalUserID, groupID);

        //Assert equals
        assertEquals(testGroupUser.getGroupUserID(), fetchedGroupUser.getGroupUserID());

    }
    
    @Test
    public void update_users_balance_test() {

        //Insert groupUser to db
        testGroupUserDAO.insertGroupUser(testGroupUser, globalUserID, groupID);

        //Create custom balance
        BigDecimal newBalance = BigDecimal.valueOf(250.22);

        //Update balance
        testGroupUserDAO.updateBalance(groupUserID, newBalance);

        //Fetching
        GroupUser fetchedGroupUser = testGroupUserDAO.withdrawGroupUser(globalUserID, groupID);

        //Assert
        assertEquals(newBalance, fetchedGroupUser.getGroupUserBalance());
    }
    
    @Test
    public void fetch_all_group_members() {
        //Insert both users to db
        testGroupUserDAO.insertGroupUser(testGroupUser, globalUserID, groupID);
        testGroupUserDAO.insertGroupUser(testGroupFriend, globalUserID, groupID);

        //Create TreeMap
        TreeMap<UUID, GroupUser> myMap = new TreeMap<>();
        myMap.put(groupUserID, testGroupUser);
        myMap.put(groupFriendID, testGroupFriend);

        //Get set of keys
        Set<UUID> keySet = myMap.keySet();

        //Fetch
        Set<UUID> fetchedSet = testGroupUserDAO.fetchGroupMembers(groupID).keySet();

        //Assert equals
        assertEquals(keySet, fetchedSet);
    }
}

package dev.ffryczek.Services;

import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.DAOs.DataSourceConnectionHelper;
import dev.ffryczek.DAOs.GlobalUserDAO;
import dev.ffryczek.DAOs.GroupDAO;
import dev.ffryczek.DAOs.GroupUserDAO;
import dev.ffryczek.DAOs.PendingRequestDAO;
import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;
import dev.ffryczek.Entities.GroupUser;
import dev.ffryczek.Entities.PendingRequest;


public class UserSessionService {

    private GlobalUserDAO globalUserDAO;
    private GroupUserDAO groupUserDAO;
    private GroupDAO groupDAO;
    private PendingRequestDAO pendingRequestDAO;

    private GlobalUser myGlobalUser;
    private TreeMap<UUID, GroupUser> myGroupUsers = new TreeMap<>();
    private TreeMap<UUID, Group> myGroups = new TreeMap<>();
    private TreeMap<UUID /* groupID */, UUID /*  my GroupUserID */> myGroupUserIDsByGroupID = new TreeMap<>();
    private TreeMap<UUID, PendingRequest> myRequests = new TreeMap<>();

    //private DataSourceConnectionHelper dataSourceHelper;

    public UserSessionService(DataSourceConnectionHelper dataSourceHelper) {
        //this.dataSourceHelper = dataSourceHelper;
        this.globalUserDAO = new GlobalUserDAO(dataSourceHelper);
        this.groupUserDAO = new GroupUserDAO(dataSourceHelper);
        this.groupDAO = new GroupDAO(dataSourceHelper);
        this.pendingRequestDAO = new PendingRequestDAO(dataSourceHelper);
    }

    //Create map GroupID - GroupUserID
    public void setMyGroupUserIDsByGroupID() {
        TreeMap<UUID, UUID> outputMap = new TreeMap<>();
        for (GroupUser gu : myGroupUsers.values()) {
            UUID assignedGroup = gu.getGroupID();
            outputMap.put(assignedGroup, gu.getGroupUserID());
        }
        this.myGroupUserIDsByGroupID = outputMap;
    }

    public TreeMap<UUID, GroupUser> getGroupUsers() {
        return new TreeMap<UUID, GroupUser>(myGroupUsers);
    }

    public TreeMap<UUID, Group> getGroups() {
        return new TreeMap<UUID, Group>(myGroups);
    }

    public TreeMap<UUID, PendingRequest> getRequests() {
        return new TreeMap<UUID, PendingRequest>(myRequests);
    }

    public UUID getMyGlobalUserID() {
        return myGlobalUser.getUserID();
    }

    public TreeMap<UUID, UUID> getMyGroupUserIDsByGroupID() {
        return new TreeMap<>(myGroupUserIDsByGroupID);
    }

    //INITIAL LOAD

    public void assignGlobalUser(String login, String password) {
        this.myGlobalUser = globalUserDAO.withdrawGlobalUser(login, password);
    }

    public void initialLoad() {
        try {
            this.myRequests = pendingRequestDAO.fetchAllRequests(myGlobalUser);
            this.myGroups = groupDAO.loadSessionGroups(myGlobalUser);
            this.myGroupUsers = groupUserDAO.loadSessionGroupUsers(myGlobalUser);
            setMyGroupUserIDsByGroupID();
        } catch (IllegalArgumentException e1) {
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
        try {
            this.myGroupUsers = groupUserDAO.loadSessionGroupUsers(myGlobalUser);
        } catch (IllegalArgumentException e2) {
            System.out.println(e2.getMessage());
            e2.printStackTrace();
        }
        try {
            this.myGroups = groupDAO.loadSessionGroups(myGlobalUser);
        } catch (IllegalArgumentException e3) {
            System.out.println(e3.getMessage());
            e3.printStackTrace();
        }
        setMyGroupUserIDsByGroupID();
    }


    //GROUP USER

    public void insertGroupUser(GroupUser groupUser, UUID groupID) {
        try {
            groupUserDAO.insertGroupUser(groupUser, myGlobalUser.getUserID(), groupID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public GroupUser withdrawGroupUser(UUID groupID) {
        try {
            return groupUserDAO.withdrawGroupUser(myGlobalUser.getUserID(), groupID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public void deleteGroupUser(GroupUser groupUser) {
        UUID groupUserID = groupUser.getGroupUserID();
        try {
            groupUserDAO.deleteGroupUser(groupUserID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteGroupUser(UUID groupUserID) {
        try {
            groupUserDAO.deleteGroupUser(groupUserID);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    //GLOBAL USER

    public GlobalUser withdrawGlobalUser(String login, String password) {
        try {
            return globalUserDAO.withdrawGlobalUser(login, password);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public UUID withdrawGlobalUser_onLogin(String usersLogin) {
        try 
        {
            return globalUserDAO.withdrawGlobalUser_onLogin(usersLogin);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
    public void insertGlobalUser(GlobalUser globalUser, String login, String password) {
        try 
        {
            globalUserDAO.insertGlobalUser(globalUser, login, password);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void deleteGlobalUser(String login, String password) {
        try {
            globalUserDAO.deleteGlobalUser(login, password);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    

    
}

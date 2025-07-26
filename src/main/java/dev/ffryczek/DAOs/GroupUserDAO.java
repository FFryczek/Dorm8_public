package dev.ffryczek.DAOs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.GroupUser;

public class GroupUserDAO {

    //Storing DataSourceConnectionHelper object
    private DataSourceConnectionHelper dataSourceHelper;

    //Constructor
    public GroupUserDAO(DataSourceConnectionHelper dataSourceHelper) {
        this.dataSourceHelper = dataSourceHelper;

    }

    //CRUD:
    //Withdraw GroupUser by UUID of globalUser and GroupID
    public GroupUser withdrawGroupUser(UUID globalUserID, UUID groupID) throws IllegalArgumentException {

        //Conversion of UUIDs to string from objects
        String GlobalUser_id = globalUserID.toString();
        String Group_id = groupID.toString();

        //Query
        String query = "SELECT * FROM GroupUser " +
                "WHERE GlobalUser_id = ? AND Group_id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 
            pStatement.setString(1, GlobalUser_id);
            pStatement.setString(2, Group_id);
            //Withdraw entry
            ResultSet resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                UUID groupUserID = UUID.fromString(resultSet.getString("id"));
                BigDecimal balance = resultSet.getBigDecimal("balance");

                //Create object of GroupUser
                GroupUser groupUser = new GroupUser(globalUserID, groupID, groupUserID, balance);
                return groupUser;
            } else {
                throw new IllegalArgumentException("\nGroupUser not found for provided user/group IDs.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }

    //Insert new groupUser using assigned globalUserID and groupID
    public void insertGroupUser(GroupUser groupUser, UUID globalUserID, UUID groupID) throws IllegalStateException {
        //Withdraw UUIDs from objects
        String id = groupUser.getGroupUserID().toString();
        String globalUser_id = globalUserID.toString();
        String group_id = groupID.toString();
        BigDecimal balance = groupUser.getGroupUserBalance();
        //Query
        String query = "INSERT INTO GroupUser (id, GlobalUser_id, Group_id, balance)" +
                "VALUES (?, ?, ?, ?)";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (id, user_name, login, password)
            pStatement.setString(1, id);
            pStatement.setString(2, globalUser_id);
            pStatement.setString(3, group_id);
            pStatement.setBigDecimal(4, balance);
            //Execture quary
            int rowsInserted = pStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("\nGroupUser sucessfully added to database!\n");
            } else {
                throw new IllegalStateException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    //Update balance of groupUser using ID
    public void updateBalance(UUID groupUserID, BigDecimal newBalance) throws IllegalArgumentException {
        String groupUser_id = groupUserID.toString();
        //Query
        String query = "UPDATE GroupUser " +
                "SET balance = ? " +
                "WHERE id = ?";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 
            pStatement.setBigDecimal(1, newBalance);
            pStatement.setString(2, groupUser_id);
            //Execture quary
            int rowsInserted = pStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("\nBalance successfully updated!\n");
            } else {
                throw new IllegalArgumentException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    //Delete groupUser by his UUID
    public void deleteGroupUser(UUID groupUserID) throws IllegalStateException {
        //Extract string from UUID
        String groupUser_id = groupUserID.toString();
        //Query
        String query = "DELETE FROM GroupUser " +
                "WHERE id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 
            pStatement.setString(1, groupUser_id);
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nGroupUser sucessfully deleted from database!\n");
            } else {
                throw new IllegalStateException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    //Cleanup Tool
    public void cleanUp(){
        //Query
        String query = "DELETE " +
                "FROM GroupUser; ";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nAll groupUsers sucessfully deleted from database!\n");
            } else {
                System.out.println("\nDeleted 0 entries (group users) \n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }

    //Fetching all GroupUsers and their IDs using groupID
    public TreeMap<UUID, GroupUser> fetchGroupMembers(UUID groupID) throws IllegalArgumentException {

        TreeMap<UUID, GroupUser> outputMap = new TreeMap<>();
        boolean found = false;
        //Get string from UUID 
        String group_id = groupID.toString();
        //Query
        String query = "SELECT * " +
                "FROM GroupUser " +
                "WHERE Group_id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 
            pStatement.setString(1, group_id);
            ResultSet resultSet = pStatement.executeQuery();

            while (resultSet.next()) {
                found = true;

                UUID newGlobalUserID = UUID.fromString(resultSet.getString("GlobalUser_id"));
                UUID newGroupUserID = UUID.fromString(resultSet.getString("id"));
                BigDecimal balance = resultSet.getBigDecimal("balance");

                GroupUser groupUser = new GroupUser(newGlobalUserID, groupID, newGroupUserID, balance);
                outputMap.put(newGroupUserID, groupUser);

            }
            if (!found) {
                throw new IllegalArgumentException("\n0 Group users found for provided group ID.\n");
            }
            return outputMap;
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }

    //Withdraw GroupUsers by UUID of globalUser (ALL GROUPUSERS BELONGING TO USER) 
    public TreeMap<UUID, GroupUser> loadSessionGroupUsers(GlobalUser globalUser) throws IllegalArgumentException {
        //Boolean for empty fetch
        Boolean found = false;
        //Create ouptut TreeMap
        TreeMap<UUID, GroupUser> outputMap = new TreeMap<>();
        //Get global User ID
        UUID globalUser_ID = globalUser.getUserID();
        //Convert UUID to string
        String globalUser_id = globalUser_ID.toString();
        //Queries
        String query = "SELECT * " +
                "FROM GroupUser " +
                "WHERE GlobalUser_id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query 1
            pStatement.setString(1, globalUser_id);
            //Withdraw entry
            ResultSet resultSet = pStatement.executeQuery();

            while (resultSet.next()) {
                found = true;

                UUID groupUserID = UUID.fromString(resultSet.getString("id"));
                BigDecimal balance = resultSet.getBigDecimal("balance");
                UUID groupID = UUID.fromString(resultSet.getString("Group_id"));

                //Create object of GroupUser
                GroupUser groupUser = new GroupUser(globalUser_ID, groupID, groupUserID, balance);

                //Put objects to treemap
                outputMap.put(groupUserID, groupUser);
            }
            if (!found) {
                throw new IllegalArgumentException("\n0 Group users found for provided user ID.\n");
            }
            return outputMap;

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }

    //Return ArrayList of GroupUsers upon List of UUIDs
    public ArrayList<GroupUser> UUIDlist2GroupUserslist(ArrayList<UUID> listID) {

        //Create ouptut 
        ArrayList<GroupUser> outputArr = new ArrayList<>();

        String query = "SELECT * " +
                "FROM GroupUser " +
                "WHERE id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            for (UUID id : listID) {
                //UUID to string
                String ID = id.toString();
                //Assign value to query 1
                pStatement.setString(1, ID);
                //Withdraw entry
                ResultSet resultSet = pStatement.executeQuery();

                if (resultSet.next()) {
                    //Withdraw values
                    UUID groupUserID = UUID.fromString(resultSet.getString("id"));
                    BigDecimal balance = resultSet.getBigDecimal("balance");
                    UUID groupID = UUID.fromString(resultSet.getString("Group_id"));
                    UUID globalUserID = UUID.fromString(resultSet.getString("GlobalUser_id"));

                    //Create object of GroupUser
                    GroupUser groupUser = new GroupUser(globalUserID, groupID, groupUserID, balance);

                    //Add object to output
                    outputArr.add(groupUser);
                }
            }
            return outputArr;
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }

    }
}

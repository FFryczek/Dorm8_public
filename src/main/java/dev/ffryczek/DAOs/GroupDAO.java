package dev.ffryczek.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.UUID;

import dev.ffryczek.Entities.GlobalUser;
import dev.ffryczek.Entities.Group;

public class GroupDAO {

    //Storing DataSourceConnectionHelper object
    private DataSourceConnectionHelper dataSourceHelper;

    //Constructor
    public GroupDAO(DataSourceConnectionHelper dataSourceHelper) {
        this.dataSourceHelper = dataSourceHelper;
    }

    //CRUD:
    //Withdraw Group by it's ID
    public Group withdrawGroup(UUID group_ID) throws IllegalArgumentException{
        String group_id = group_ID.toString();

        String query = "SELECT * FROM `Group` " +
                "WHERE id = ?;";
        try (
                Connection conn = dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query
            pStatement.setString(1, group_id);
            //Withdraw entry
            ResultSet resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");

                //Create object of GlobalUser
                Group returnedGroup = new Group(name, group_ID);
                return returnedGroup;
            } else {
                throw new IllegalArgumentException("\nCannot find matching group\n");
            }
        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }
    
    //Insert new droup to DB
    public void insertGroup(Group group) throws IllegalArgumentException{

        //Withdraw UUID and name from objects
        String id = group.getGroupID().toString();
        String name = group.getName();
        //Query
        String query = "INSERT INTO `Group` (id, name)" +
                "VALUES (?, ?)";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (id, user_name, login, password)
            pStatement.setString(1, id);
            pStatement.setString(2, name);

            //Execture quary
            int rowsInserted = pStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("\nGroup sucessfully added to database!\n");
            } else {
                throw new IllegalArgumentException("\nOperation failed.\n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }
    
    //Delete group from DB by it's UUID
    public void deleteGroup(UUID groupID) throws IllegalStateException {
        //Extract string from UUID
        String group_id = groupID.toString();
        //Query
        String query = "DELETE FROM `Group` " +
                "WHERE id = ?;";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            //Assign value to query (login and password)
            pStatement.setString(1, group_id);
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nGroup sucessfully deleted from database!\n");
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
                "FROM `Group`; ";
        try (
                Connection conn = this.dataSourceHelper.openConnection();
                PreparedStatement pStatement = conn.prepareStatement(query);

        ) {
            int rowsUpdated = pStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nAll groups sucessfully deleted from database!\n");
            } else {
                System.out.println("\nDeleted 0 entries (groups) \n");
            }

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
        }
    }
 
    //Withdraw all Groups that GlobalUser belongs to
    public TreeMap<UUID, Group> loadSessionGroups(GlobalUser globalUser) throws IllegalArgumentException {
        //Boolean for empty fetch
        Boolean found = false;
        //Create ouptut TreeMap
        TreeMap<UUID,Group> outputMap = new TreeMap<>();
        //Get global User ID
        UUID globalUser_ID = globalUser.getUserID();
        //Convert UUID to string
        String globalUser_id = globalUser_ID.toString();
        //Queries
        String query = "SELECT " +
                "`Group`.id AS id, " +
                "`Group`.name AS name "+
                "FROM `Group` " +
                "JOIN GroupUser " +
                "ON `Group`.id = GroupUser.Group_id "+
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

                UUID groupID = UUID.fromString(resultSet.getString("id"));
                String name = resultSet.getString("name");


                //Create object of GroupUser
                Group group = new Group(name, groupID);


                //Put objects to treemap
                outputMap.put(groupID,group);
            }
            if (!found) {
                throw new IllegalArgumentException("\n0 Groups found for provided user ID.\n");
            }
            return outputMap;

        } catch (SQLException e) {
            System.out.println("\nSomething went wrong when opening connection\n");
            e.printStackTrace();
            return null;
        }
    }
    
}
